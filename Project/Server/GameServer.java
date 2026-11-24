package Project.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path; 
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.TimedEvent;
import Project.Common.ValidationUtils;
import Project.Exceptions.ValidationException;


/**
 * Concrete game session scaffold based on the old GameRoom lifecycle.
 *
 * Commands currently supported from clients:
 * - /ready
 * - /turn <action>
 */
public class GameServer extends BaseGameServer {

    private static final int MIN_PLAYERS_TO_START = 2;
    private static final int READY_SECONDS = 30;
    private static final int ROUND_SECONDS = 30;
    private static final int MAX_ROUNDS = 3;
    private static final int TURN_SECONDS = 20;
    private static final String GAME_TAG = "[Game] ";

    private volatile Phase phase = Phase.INACTIVE;
    private volatile TimedEvent readyTimer;
    private volatile TimedEvent roundTimer;
    private volatile TimedEvent turnTimer;
    private volatile Long currentTurnPlayerId;
    private int roundNumber = 0;

    /**JA - 11/19/2026: Stores the Hangman words and guessing info. */
    private List <String> words = new ArrayList<>();
    private String hiddenWord = "";
    private String visibleWord = "";
    private Set <Character> guessedLetters = new HashSet<>();
    private int tries = 0; 
    private List<Long> turnOrder = new ArrayList<>();
    private static final int MAX_TRIES = 6; 
    private boolean wordFinished = false; 

    /** JA- 11/19/26: Loads Hangman words based on the word.txt file */

    private void loadWords() {
        words.clear();

        try {
            List<String> lines = Files.readAllLines(Path.of("Project/Server/words.txt"));

            for (String line : lines) {
                String word = line.trim().toLowerCase();

                if (word.matches("[a-z]+")) {
                    words.add(word);
                }
            }
            if (words.size() < 5) { 
                throw new IllegalStateException("You need at least 5 words for this game");
            }

            LoggerUtil.INSTANCE.info("[Game] Loaded" + words.size() + "words. ");
        } catch (IOException e) {
            throw new IllegalStateException("Could not load Hangman words");
        }
    }

    // start region for lifecycle hook implementations
    @Override
    protected void onSpectatorJoined(ServerThread client) {
        if (client == null) {
            return;
        }
        if (phase == Phase.INACTIVE) {
            return; // nothing to sync if the session isn't active
        }
        LoggerUtil.INSTANCE.info("[GameServer] Spectator joined: " + client.getDisplayName());
        unicastGameMessage(client, "Joined as spectator. Current active players: " + getActivePlayerCount());
        unicastGameStateToJoiner(client);
    }

    @Override
    protected void onPlayerJoined(ServerThread client) {
        if (client == null) {
            return;
        }
        if (phase == Phase.INACTIVE) {
            return; // nothing to sync if the session isn't active
        }
        LoggerUtil.INSTANCE.info("[GameServer] Player joined via ready: " + client.getDisplayName());
        unicastGameMessage(client, "Joined as active player. Waiting room status: "
                + getActivePlayerCount() + "/" + MIN_PLAYERS_TO_START + " ready.");
        if (phase != Phase.READY) {
            unicastGameStateToJoiner(client);
        }
        broadcastGameMessage(client.getDisplayName() + " joined active players.");
        broadcastGameMessage("Active players: " + getActivePlayerCount());
    }

    @Override
    protected void onPlayerLeft(ServerThread client) {
        if (client == null) {
            return;
        }

        LoggerUtil.INSTANCE.info("[GameServer] Player left: " + client.getDisplayName());
        if (getActivePlayerCount() == 0) {
            resetReadyTimer();
            onSessionEnd();
            return;
        }
        if (phase == Phase.IN_PROGRESS && getActivePlayerCount() < MIN_PLAYERS_TO_START) {
            broadcastGameMessage("Not enough active players to continue.");
            onSessionEnd();
        }
    }

    /**    
     * JA- 11/19/26
     * Starts a Hangman sessions and gets everything set up for the first word
     */

    @Override
    protected synchronized void onSessionStart() {
        LoggerUtil.INSTANCE.info("[GameServer] onSessionStart() start");
        resetReadyTimer();
        roundNumber = 0;
        wordFinished = false;
        
        //JA- Adding the load for the word list
        loadWords();

        //JA- Picks a random hidden word
        hiddenWord = words.get (new Random().nextInt(words.size()));

        //JA- Creates that underline design to show the blank letter
        visibleWord = "_".repeat(hiddenWord.length());

        //JA - Resets your tries and guessed letters too
        tries = 0;
        guessedLetters.clear();

        //JA- resets users scores
        for (ServerThread player : getActivePlayers()) {
            player.setPoints(0);
            broadcastPlayerPoints(player);
        }

        //JA- Changed Messages
        broadcastGameMessage("Time to Play Some Hangman!");
        broadcastGameMessage("The word for today: " + visibleWord);
        broadcastGameMessage("Attempts: " + tries + "/" + MAX_TRIES);

        //JA- 11/19/26- Give users turns to play
        turnOrder.clear();
        for (ServerThread player : getActivePlayers()) {
            turnOrder.add(player.getClientId());
        }

        Collections.sort(turnOrder);
        Collections.shuffle(turnOrder);

        broadcastGameMessage("PLayer turn order has been selected");
        
        LoggerUtil.INSTANCE.info("[GameServer] onSessionStart() end");
        onRoundStart();
    }

    /**
     * JA - 11/19/2026
     * Starts a new Hangman round and resets player turns.
     */

    @Override
    protected synchronized void onRoundStart() {
        LoggerUtil.INSTANCE.info("[GameServer] onRoundStart() start");
        resetRoundTimer();
        phase = Phase.IN_PROGRESS; // toggle from READY or EVALUATION
        broadcastCurrentPhase();
       
       
        for (ServerThread player : getActivePlayers()) {
            player.setTurnTaken(false);
            broadcastTurnStatus(player.getClientId(), false);
        }

        //JA 11/19/26- Helps with the next rounds
        roundNumber++; 
       
        broadcastGameMessage("Hangman: Round " + roundNumber + " has started!");

        if (!wordFinished) {
            broadcastGameMessage("Your word for the day:" + visibleWord);
            broadcastGameMessage("Attempts: " + tries + "/" + MAX_TRIES);
        }

        LoggerUtil.INSTANCE.info("[GameServer] onRoundStart() end");
        
       //JA- 11/19/26- Needed so that the first player can take their turn
        onTurnStart();  
        
    }

    /**
     * JA - 11/19/2026
     * Starts next turn for Player when playing Hangman
     */

    @Override
    protected synchronized void onTurnStart() {
        LoggerUtil.INSTANCE.info("[GameServer] onTurnStart() start");
        resetTurnTimer();

        if (phase != Phase.IN_PROGRESS) {
            return;
        }

        //JA- 11/19/26 -Sees player who has not taken turn yet
        for (Long playerId : turnOrder) {
            ServerThread player = activePlayers.get(playerId);

            if ( player != null && !player.isTurnTaken()) {
                currentTurnPlayerId = player.getClientId();

                if (wordFinished) {
                    startNewWord();
                }

                broadcastGameMessage("It's " + player.getDisplayName() + "'s turn to guess a letter.");
                broadcastGameMessage("USer /letter <letter> or /guess <word> to see if you can guess the word.");
                startTurnTimer();

                LoggerUtil.INSTANCE.info("[GameServer] onTurnStart() end");
                return;
            }
        }
        //JA 11/19/26- Once they get all turns, round ends
        onRoundEnd();

    }

    @Override
    protected synchronized void onTurnEnd() {
        LoggerUtil.INSTANCE.info("[GameServer] onTurnEnd() start");
        
        if (phase != Phase.IN_PROGRESS || currentTurnPlayerId == null) {
            return;
        }

        resetTurnTimer();

        //JA- 11/19/26 - Sees who is the last player whose turn just ended
        ServerThread player = activePlayers.get(currentTurnPlayerId);

        if (player != null && !player.isTurnTaken()) {
            player.setTurnTaken(true);
            broadcastTurnStatus(player.getClientId(), true);

            broadcastGameMessage(player.getDisplayName() + "'s turn has ended.");
        }

        currentTurnPlayerId = null;

        //JA 11/19/26- Checks to see if everyone finished their turns
        boolean allTaken = true; 

        for (ServerThread activePlayer : getActivePlayers()) {
            if (!activePlayer.isTurnTaken()) {
                allTaken = false;
                break;
            }
        }

        LoggerUtil.INSTANCE.info("[GameServer] onTurnEnd() end");
        if( allTaken) {
            onRoundEnd();
        } else {
            onTurnStart();
        }

    }
    
    /**
     * JA - 11/19/2026
     * Ends a Hangman round and shows score, so that it can
     * starts the next round or finishes the session.
     */

    @Override
    protected synchronized void onRoundEnd() {
        LoggerUtil.INSTANCE.info("[GameServer] onRoundEnd() start");

        //JA 11/19/26- Do not let round from ending more than once

        if (phase != Phase.IN_PROGRESS) {
            return;
        }

        phase = Phase.EVALUATION;
        broadcastCurrentPhase();

        resetRoundTimer();
        resetTurnTimer();
        currentTurnPlayerId = null;

        broadcastGameMessage("Round " + roundNumber + " has ended!");

        //JA 11/19/26 - Organize winners and get their names

        List<ServerThread> players = new ArrayList<>(getActivePlayers());
        players.sort((p1, p2) -> { 
            int result = Integer.compare(p2.getPoints(), p1.getPoints());

            if (result != 0) {
                return result;
            }

            return Long.compare(p1.getClientId(), p2.getClientId());
        });

        broadcastGameMessage("-----Scoreboard-----");

        for (ServerThread player : players) {
            broadcastGameMessage(player.getDisplayName() + ": " + player.getPoints() + " points");
        }

        broadcastGameMessage("--------------------");

        LoggerUtil.INSTANCE.info("GameServer onRoundEnd() end");

        //JA 11/19/26- Sees whether all rounds have finished
        if (roundNumber >= MAX_ROUNDS) {
            onSessionEnd();
        } else {
            onRoundStart();
        }

    }

    @Override
    protected synchronized void onSessionEnd() {
        LoggerUtil.INSTANCE.info("[GameServer] onSessionEnd() start");
        resetReadyTimer();
        resetTurnTimer();
        resetRoundTimer();
        wordFinished = false;

        currentTurnPlayerId = null;
        phase = Phase.INACTIVE;

        List<ServerThread> snapshot = new ArrayList<>(getActivePlayers());
        // find user with highest score; they're the winner (uses stream api)
        snapshot.sort((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()));

        broadcastGameMessage("~~~~~ Final RESULTS ~~~~~");
        for (ServerThread player : snapshot) {
            broadcastGameMessage(player.getDisplayName() + ": " + player.getPoints() + " points");
        }
        broadcastGameMessage("~~~~~~~~~~~~~~~~~~~~~~~");

        //JA 11/19/26- Shows who won the game 
        if (snapshot.isEmpty()) { 
            broadcastGameMessage("No player has won the game.");

        } else { 
            int highestScore = snapshot.get(0).getPoints();
            List<String> winners = new ArrayList<>();

            for (ServerThread player :snapshot) {
                if (player.getPoints() == highestScore) {
                    winners.add(player.getDisplayName());
                }
            }

            if (winners.size() == 1) {
                broadcastGameMessage(winners.get(0) + " won with " + highestScore + " points!");
            } else {
                broadcastGameMessage("A tie! Between " + String.join(", ", winners) + " with " + highestScore + " points!");
            }
        }

        //JA 11/20/26
        hiddenWord = "";
        visibleWord = "";
        guessedLetters.clear();
        tries = 0;
        turnOrder.clear();
        roundNumber = 0; 

        // reset player data and sync changes to clients before clearing active players,
        // so that clients have a chance to update any relevant UI (like ready status)
        // before being removed from the session
        for (ServerThread player : snapshot) {
            player.resetGameState();
        }
        // default client id is used as a reset trigger, no need to individually sync
        // resets for each property
        broadcastReadyStatus(Constants.DEFAULT_CLIENT_ID, false);
        clearActivePlayers();

        broadcastCurrentPhase();
        broadcastGameMessage("Session ended. Type /ready to join the next session.");
        LoggerUtil.INSTANCE.info("[GameServer] onSessionEnd() end");
    }
    // end region for lifecycle hook implementations

    // Start region for timer handlers ===================================

    private synchronized void startReadyTimer(boolean resetOnTry) {
        if (phase != Phase.READY) {
            return;
        }
        if (resetOnTry) {
            resetReadyTimer();
        }
        if (readyTimer == null) {
            readyTimer = new TimedEvent(READY_SECONDS, this::checkReadyStatus);
            readyTimer.setTickCallback(time -> LoggerUtil.INSTANCE.info("[GameServer] Ready timer: " + time));
            broadcastGameMessage(
                    "Ready check started. Session begins in " + READY_SECONDS + "s if enough players are ready.");
        }
    }

    private synchronized void resetReadyTimer() {
        if (readyTimer != null) {
            readyTimer.cancel();
            readyTimer = null;
        }
    }

    private synchronized void startRoundTimer() {
        roundTimer = new TimedEvent(ROUND_SECONDS, this::onRoundEnd);
        roundTimer.setTickCallback(time -> LoggerUtil.INSTANCE.info("[GameServer] Round timer: " + time));
    }

    private synchronized void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
        }
    }

    private synchronized void startTurnTimer() {
        turnTimer = new TimedEvent(TURN_SECONDS, this::onTurnEnd);
        turnTimer.setTickCallback(time -> LoggerUtil.INSTANCE.info("[GameServer] Turn timer: " + time));
    }

    private synchronized void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }

    // End region for timer handlers ===================================
    private synchronized void checkReadyStatus() {
        if (phase != Phase.READY) {
            return;
        }
        if (getActivePlayerCount() >= MIN_PLAYERS_TO_START) {
            onSessionStart();
        } else {
            broadcastGameMessage("Ready check expired: not enough ready players.");
            onSessionEnd();
        }
    }

    // start region for handle*() methods called by Server

    protected void handleGuess(ServerThread sender, String letter) {
        try {
            ValidationUtils.requireParticipating(isActivePlayer(sender));
            ValidationUtils.requirePhase(phase, Phase.IN_PROGRESS);
            ValidationUtils.requireTurnNotTaken(sender.isTurnTaken());
            ValidationUtils.requireCurrentPlayer(currentTurnPlayerId, sender.getClientId());

            //JA 11/20/26 -Check if input within alphabet

            String word = ValidationUtils.requireValidTurnOption(letter);

            if (wordFinished) {
                unicastGameMessage(sender, "The word has been completed.");
                return;
            }

            //JA 11/20/26- Counts to see what letters are hidden still & rewards per find + bonus
            if(word.equals(hiddenWord)) {
                int missingLetters = 0; 

                for (int i = 0; i < visibleWord.length(); i++) {
                    if (visibleWord.charAt(i) == '_') {
                        missingLetters++;
                    }
                }

                int pointsEarned = missingLetters + (missingLetters * 2); 

                sender.setPoints(sender.getPoints() + pointsEarned);
                broadcastPlayerPoints(sender); 

                visibleWord = hiddenWord;
                wordFinished = true;

                broadcastGameMessage(sender.getDisplayName() + " has found the word : " + hiddenWord);
                broadcastGameMessage("Points earned: " + pointsEarned);
                broadcastGameMessage("The word was: " + visibleWord);

            } else { 
                //JA 11/20/26 - Getting wrong letter gets you a retry + a warning

                tries++; 

                broadcastGameMessage(sender.getDisplayName() + " guessed " + word + " incorrectly... shame on you.");
                broadcastGameMessage("Attempts: " + tries + "/" + MAX_TRIES);

                if (tries >= MAX_TRIES) { 
                    wordFinished = true;
                    broadcastGameMessage("You ran out of tries... no more.");
                    broadcastGameMessage("The word was: " + hiddenWord);

                }
            }

            onTurnEnd();


        } catch (ValidationException e) {
            LoggerUtil.INSTANCE.warning("[GameServer] " + e.getMessage());
            unicastGameMessage(sender, e.getMessage());
        }

    }

    /**
     * JA - 11/20/2026
     * Checks a letter and updates it.
     */
    
    protected void handleLetter(ServerThread sender, String letter) { 
        try {
            ValidationUtils.requireParticipating(isActivePlayer(sender));
            ValidationUtils.requirePhase(phase, Phase.IN_PROGRESS);
            ValidationUtils.requireTurnNotTaken(sender.isTurnTaken());
            ValidationUtils.requireCurrentPlayer(currentTurnPlayerId, sender.getClientId());
            String input = ValidationUtils.requireValidTurnOption(letter);

            if (input.length() != 1) { 
                unicastGameMessage(sender, "Please just put in ONE letter.");
                return;
            }

            char guessedLetter = input.charAt(0);

            //JA 11/20/26 - Letters should only be used once, so reject already attempted ones
            if (guessedLetters.contains(guessedLetter)) {
                unicastGameMessage(sender, "You already guessed this letter, you bum.");
                return;
            }

            guessedLetters.add(guessedLetter);

            char[] updatedWord = visibleWord.toCharArray();
            int matches = 0;

            //JA 11/20/26- Once guessed right, letter gets revealed

            for (int i = 0; i < hiddenWord.length(); i++) {
                if (hiddenWord.charAt(i) == guessedLetter && updatedWord[i] == '_') {
                    updatedWord[i] = guessedLetter;
                    matches++;
                }
            }

            if (matches > 0) {
                visibleWord = new String(updatedWord);
                sender.setPoints(sender.getPoints() + matches);
                broadcastPlayerPoints(sender);

                broadcastGameMessage(sender.getDisplayName() + " has guessed the letter '" + guessedLetter + "' correctly!");
                broadcastGameMessage("Found " + matches + " new letter(s)"); 
                broadcastGameMessage("You earned: " + matches + " points. Well Done!");
                broadcastGameMessage("The word: " + visibleWord);

                if (!visibleWord.contains("_")) {
                    wordFinished = true;
                    broadcastGameMessage("Congrats! You finished the word: " + hiddenWord);
                }
            } else { 
                tries++; 

                broadcastGameMessage(sender.getDisplayName() + " guessed " + guessedLetter + " incorrectly, shame... shame.");
                broadcastGameMessage("Attempts: " + tries + "/" + MAX_TRIES);

                if (tries >= MAX_TRIES) {
                    wordFinished = true;
                    broadcastGameMessage("The word was: " + hiddenWord + " you ran out you bum.");
                }
            }

            onTurnEnd();

        } catch (ValidationException e) {
            LoggerUtil.INSTANCE.warning("[GameServer] " + e.getMessage());
            unicastGameMessage(sender, e.getMessage());
        }
    }

    private void startNewWord() { 
        hiddenWord = words.get(new Random().nextInt(words.size()));
        visibleWord = "_".repeat(hiddenWord.length());

        tries = 0;
        guessedLetters.clear();
        wordFinished = false;

        broadcastGameMessage("Time for the new word!");
        broadcastGameMessage("The word is: " + visibleWord);
        broadcastGameMessage("Attempts: " + tries + "/" + MAX_TRIES);
    }

    /**
     * Handles a player's ready action. Validates the action, registers them as an
     * active player, and starts the ready timer. Sends an error message back to the
     * player on failure.
     */
    public void handleReady(ServerThread sender) {
        try {
            ValidationUtils.requirePhaseAtMost(phase, Phase.READY);
            ValidationUtils.requireNotAlreadyReady(isActivePlayer(sender));

            if (phase == Phase.INACTIVE) {
                phase = Phase.READY;
                broadcastCurrentPhase();
            }

            sender.resetGameState();
            sender.setReady(true);
            if (addActivePlayer(sender)) {
                onPlayerJoined(sender);
            }

            broadcastReadyStatus(sender.getClientId(), true);
            broadcastGameMessage(sender.getDisplayName() + " is ready. (" + getActivePlayerCount() + " active)");
            startReadyTimer(false);
        } catch (ValidationException e) {
            LoggerUtil.INSTANCE.warning("[GameServer] " + e.getMessage());
            unicastGameMessage(sender, e.getMessage());
        }
    }

    /**
     * Handles a player's turn action. Validates the action, records the turn, and
     * advances the game. Sends an error message back to the player on failure.
     */
    @Deprecated
    public void handleTurn(ServerThread sender, String action) {
        try {
            ValidationUtils.requireParticipating(isActivePlayer(sender));
            ValidationUtils.requirePhase(phase, Phase.IN_PROGRESS);
            ValidationUtils.requireTurnNotTaken(sender.isTurnTaken());
            String normalizedAction = ValidationUtils.requireValidTurnOption(action);

            // TODO: turn logic would go here, in this example we're just marking that we
            // took a turn
            // ValidationUtils.requireCurrentPlayer(currentTurnPlayerId,
            // sender.getClientId());

            sender.setTurnTaken(true);
            broadcastTurnStatus(sender.getClientId(), true);
            onTurnEnd();
        } catch (ValidationException e) {
            LoggerUtil.INSTANCE.warning("[GameServer] " + e.getMessage());
            unicastGameMessage(sender, e.getMessage());
        }
    }

    // end region for handle*() methods called by Server

    // start region for helper methods to send data to clients

    private void broadcastPointsReset() { // optional reset for specific property, but we'll leverage the READY reset as
                                          // a full reset for simplicity in this example
        Server.INSTANCE.sendOrDisconnect(serverThread -> serverThread.sendPlayerPoints(Constants.DEFAULT_CLIENT_ID, 0));
    }

    private void broadcastPlayerPoints(ServerThread player) {
        if (player == null) {
            return;
        }
        Server.INSTANCE.sendOrDisconnect(
                serverThread -> serverThread.sendPlayerPoints(player.getClientId(), player.getPoints()));
    }

    private void unicastPlayerPoints(ServerThread target, long clientId, int points) {
        if (target == null) {
            return;
        }
        Server.INSTANCE.unicast(target, serverThread -> serverThread.sendPlayerPoints(clientId, points));
    }

    private void unicastGuessConfirmation(ServerThread target, int guess) {
        Server.INSTANCE.unicast(target, serverThread -> serverThread.sendGuessConfirmation(guess));
    }

    /**
     * Sends the current phase plus all existing active players' ready, turn, and
     * points state to a newly joined player.
     */
    private void unicastGameStateToJoiner(ServerThread joiner) {
        if (joiner == null) {
            return;
        }
        unicastCurrentPhase(joiner);

        List<ServerThread> snapshot = new ArrayList<>(getActivePlayers());
        
        for (ServerThread player : snapshot) {
            if (player.getClientId() == joiner.getClientId()) {
                continue;
            }
            unicastReadyStatus(joiner, player.getClientId(), player.isReady());
            unicastTurnStatus(joiner, player.getClientId(), player.isTurnTaken());
            unicastPlayerPoints(joiner, player.getClientId(), player.getPoints());
        }

        //JA 11/20/26- Gives later joiners the ability to see the current word and attempts

        if (phase == Phase.IN_PROGRESS || phase == Phase.EVALUATION) {
            unicastGameMessage(joiner, "===== Current GamePlay =====");
            unicastGameMessage(joiner, "Round: " + roundNumber + "/" + MAX_ROUNDS);
            unicastGameMessage(joiner, "Word: " + visibleWord);
            unicastGameMessage(joiner, "Attempts: " + tries + "/" + MAX_TRIES);
        }

        if (currentTurnPlayerId != null) {
            ServerThread currentPlayer = activePlayers.get(currentTurnPlayerId);
            if (currentPlayer != null) {
                unicastGameMessage(joiner, "Current turn: " + currentPlayer.getDisplayName());
            }
        }

        unicastGameMessage(joiner, "You are spectating the game.");
        unicastGameMessage(joiner, "============================");
    }

    /** Sends the current game phase to all connected clients. */
    private void broadcastCurrentPhase() {
        Server.INSTANCE.sendOrDisconnect(serverThread -> serverThread.sendGamePhase(phase));
    }

    /** Sends the current game phase to a single client. */
    private void unicastCurrentPhase(ServerThread target) {
        Server.INSTANCE.unicast(target, serverThread -> serverThread.sendGamePhase(phase));
    }

    /** Notifies all connected clients of a player's ready status. */
    private void broadcastReadyStatus(long clientId, boolean isReady) {
        Server.INSTANCE.sendOrDisconnect(serverThread -> serverThread.sendReadyStatus(clientId, isReady));
    }

    /** Sends a player's ready status to a single client. */
    private void unicastReadyStatus(ServerThread target, long clientId, boolean isReady) {
        Server.INSTANCE.unicast(target, serverThread -> serverThread.sendReadyStatus(clientId, isReady));
    }

    /** Notifies all connected clients of a player's turn-taken status. */
    private void broadcastTurnStatus(long clientId, boolean hasTakenTurn) {
        Server.INSTANCE.sendOrDisconnect(serverThread -> serverThread.sendTurnStatus(clientId, hasTakenTurn));
    }

    /** Sends a player's turn-taken status to a single client. */
    private void unicastTurnStatus(ServerThread target, long clientId, boolean hasTakenTurn) {
        Server.INSTANCE.unicast(target, serverThread -> serverThread.sendTurnStatus(clientId, hasTakenTurn));
    }

    /** Sends a game message to all connected clients. */
    private void broadcastGameMessage(String message) {
        Server.INSTANCE.broadcast(null, GAME_TAG + message);
    }

    /** Sends a game message to a single client. */
    private void unicastGameMessage(ServerThread target, String message) {
        if (target == null) {
            return;
        }
        final String formatted = GAME_TAG + message;
        Server.INSTANCE.unicast(target, serverThread -> serverThread.sendMessage(formatted));
    }

    // end region for helper methods to send data to clients
}