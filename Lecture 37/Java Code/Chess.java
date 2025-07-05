import java.util.*;

// Enums for better type safety
enum Color {
    WHITE, BLACK
}

enum PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
}

enum GameStatus {
    WAITING, IN_PROGRESS, COMPLETED, ABORTED
}

// Position class to represent coordinates
class Position {
    private int row;
    private int col;

    public Position() {
        row = 0;
        col = 0;
    }
    
    public Position(int r, int c) {
        row = r;
        col = c;
    }
    
    public int getRow() { 
        return row; 
    }
    public int getCol() { 
        return col; 
    }
    
    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position other = (Position) obj;
        return row == other.row && col == other.col;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    
    public int compareTo(Position other) {
        if (row != other.row) return Integer.compare(row, other.row);
        return Integer.compare(col, other.col);
    }
    
    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
    
    // Convert to chess notation (e.g., e4, f7)
    public String toChessNotation() {
        char file = (char)('a' + col);
        char rank = (char)('8' - row);
        return "" + file + rank;
    }
}

// Move class to represent a chess move
class Move {
    private Position from;
    private Position to;
    private Piece piece;
    private Piece capturedPiece;

    public Move() {
        piece = null;
        capturedPiece = null;
    }
    
    public Move(Position f, Position t, Piece p, Piece captured) {
        from = f;
        to = t;
        piece = p;
        capturedPiece = captured;
    }
    
    public Position getFrom() { 
        return from; 
    }
    public Position getTo() { 
        return to; 
    }
    public Piece getPiece() { 
        return piece; 
    }
    public Piece getCapturedPiece() { 
        return capturedPiece; 
    }
}

// Abstract Piece class following Strategy Pattern
abstract class Piece {
    protected Color color;
    protected PieceType type;
    protected boolean hasMoved;

    public Piece(Color c, PieceType t) {
        color = c;
        type = t;
        hasMoved = false;
    }
    
    public Color getColor() { 
        return color; 
    }
    public PieceType getType() { 
        return type; 
    }
    public boolean getHasMoved() { 
        return hasMoved; 
    }
    public void setMoved(boolean moved) { 
        hasMoved = moved; 
    }
    
    public abstract List<Position> getPossibleMoves(Position currentPos, Board board);
    public abstract String getSymbol();
    
    public String toString() {
        String colorStr = (color == Color.WHITE) ? "W" : "B";
        return colorStr + getSymbol();
    }
}

// Concrete Piece implementations
class King extends Piece {
    public King(Color color) { 
        super(color, PieceType.KING); 
    }
    
    @Override
    public List<Position> getPossibleMoves(Position currentPos, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        
        for (int i = 0; i < 8; i++) {
            Position newPos = new Position(currentPos.getRow() + directions[i][0], currentPos.getCol() + directions[i][1]);
            if (newPos.isValid() && !board.isOccupiedBySameColor(newPos, this.color)) {
                moves.add(newPos);
            }
        }
        return moves;
    }
    
    @Override
    public String getSymbol() { 
        return "K"; 
    }
}

class Queen extends Piece {
    public Queen(Color color) { 
        super(color, PieceType.QUEEN); 
    }
    
    @Override
    public List<Position> getPossibleMoves(Position currentPos, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{-1,-1}, {-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        
        for (int d = 0; d < 8; d++) {
            for (int i = 1; i < 8; i++) {
                Position newPos = new Position(currentPos.getRow() + directions[d][0]*i, currentPos.getCol() + directions[d][1]*i);
                if (!newPos.isValid()) break;

                if (board.isOccupiedBySameColor(newPos, this.color)) break;

                moves.add(newPos);
                if (board.isOccupied(newPos)) break; // Stop after capturing
            }
        }
        return moves;
    }
    
    @Override
    public String getSymbol() { 
        return "Q"; 
    }
}

class Rook extends Piece {
    public Rook(Color color) { 
        super(color, PieceType.ROOK); 
    }
    
    @Override
    public List<Position> getPossibleMoves(Position currentPos, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        
        for (int d = 0; d < 4; d++) {
            for (int i = 1; i < 8; i++) {
                Position newPos = new Position(currentPos.getRow() + directions[d][0]*i, currentPos.getCol() + directions[d][1]*i);
                if (!newPos.isValid()) break;

                if (board.isOccupiedBySameColor(newPos, this.color)) break;

                moves.add(newPos);
                if (board.isOccupied(newPos)) break;
            }
        }
        return moves;
    }
    
    @Override
    public String getSymbol() { 
        return "R"; 
    }
}

class Bishop extends Piece {
    public Bishop(Color color) { 
        super(color, PieceType.BISHOP); 
    }
    
    @Override
    public List<Position> getPossibleMoves(Position currentPos, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};
        
        for (int d = 0; d < 4; d++) {
            for (int i = 1; i < 8; i++) {
                Position newPos = new Position(currentPos.getRow() + directions[d][0]*i, currentPos.getCol() + directions[d][1]*i);
                if (!newPos.isValid()) break;
                if (board.isOccupiedBySameColor(newPos, this.color)) break;
                moves.add(newPos);
                if (board.isOccupied(newPos)) break;
            }
        }
        return moves;
    }
    
    @Override
    public String getSymbol() { 
        return "B"; 
    }
}

class Knight extends Piece {
    public Knight(Color color) { 
        super(color, PieceType.KNIGHT); 
    }
    
    @Override
    public List<Position> getPossibleMoves(Position currentPos, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] knightMoves = {{-2,-1}, {-2,1}, {-1,-2}, {-1,2}, {1,-2}, {1,2}, {2,-1}, {2,1}};
        
        for (int i = 0; i < 8; i++) {
            Position newPos = new Position(currentPos.getRow() + knightMoves[i][0], currentPos.getCol() + knightMoves[i][1]);
            if (newPos.isValid() && !board.isOccupiedBySameColor(newPos, this.color)) {
                moves.add(newPos);
            }
        }
        return moves;
    }
    
    @Override
    public String getSymbol() { 
        return "N"; 
    }
}

class Pawn extends Piece {
    public Pawn(Color color) { 
        super(color, PieceType.PAWN); 
    }
    
    @Override
    public List<Position> getPossibleMoves(Position currentPos, Board board) {
        List<Position> moves = new ArrayList<>();
        int direction = (color == Color.WHITE) ? -1 : 1;
        
        // Forward move
        Position oneStep = new Position(currentPos.getRow() + direction, currentPos.getCol());
        if (oneStep.isValid() && !board.isOccupied(oneStep)) {
            moves.add(oneStep);
            
            // Double move from starting position
            if (!hasMoved) {
                Position twoStep = new Position(currentPos.getRow() + 2*direction, currentPos.getCol());
                if (twoStep.isValid() && !board.isOccupied(twoStep)) {
                    moves.add(twoStep);
                }
            }
        }
        
        // Diagonal captures
        Position leftCapture = new Position(currentPos.getRow() + direction, currentPos.getCol() - 1);
        Position rightCapture = new Position(currentPos.getRow() + direction, currentPos.getCol() + 1);
        
        if (leftCapture.isValid() && board.isOccupied(leftCapture) && 
            !board.isOccupiedBySameColor(leftCapture, this.color)) {
            moves.add(leftCapture);
        }
        
        if (rightCapture.isValid() && board.isOccupied(rightCapture) && 
            !board.isOccupiedBySameColor(rightCapture, this.color)) {
            moves.add(rightCapture);
        }
        
        return moves;
    }
    
    @Override
    public String getSymbol() { 
        return "P"; 
    }
}

// Factory Pattern for creating pieces
class PieceFactory {
    public static Piece createPiece(PieceType type, Color color) {
        switch (type) {
            case KING: return new King(color);
            case QUEEN: return new Queen(color);
            case ROOK: return new Rook(color);
            case BISHOP: return new Bishop(color);
            case KNIGHT: return new Knight(color);
            case PAWN: return new Pawn(color);
            default: return null;
        }
    }
}

// Board class - Dumb object that manages pieces
class Board {
    private Piece[][] board;
    private Map<Position, Piece> piecePositions;

    public Board() {
        // Initialize board to null
        board = new Piece[8][8];
        piecePositions = new HashMap<>();
        initializeBoard();
    }
    
    public void initializeBoard() {
        // Initialize white pieces
        placePiece(new Position(7, 0), PieceFactory.createPiece(PieceType.ROOK, Color.WHITE));
        placePiece(new Position(7, 1), PieceFactory.createPiece(PieceType.KNIGHT, Color.WHITE));
        placePiece(new Position(7, 2), PieceFactory.createPiece(PieceType.BISHOP, Color.WHITE));
        placePiece(new Position(7, 3), PieceFactory.createPiece(PieceType.QUEEN, Color.WHITE));
        placePiece(new Position(7, 4), PieceFactory.createPiece(PieceType.KING, Color.WHITE));
        placePiece(new Position(7, 5), PieceFactory.createPiece(PieceType.BISHOP, Color.WHITE));
        placePiece(new Position(7, 6), PieceFactory.createPiece(PieceType.KNIGHT, Color.WHITE));
        placePiece(new Position(7, 7), PieceFactory.createPiece(PieceType.ROOK, Color.WHITE));
        
        for (int i = 0; i < 8; i++) {
            placePiece(new Position(6, i), PieceFactory.createPiece(PieceType.PAWN, Color.WHITE));
        }
        
        // Initialize black pieces
        placePiece(new Position(0, 0), PieceFactory.createPiece(PieceType.ROOK, Color.BLACK));
        placePiece(new Position(0, 1), PieceFactory.createPiece(PieceType.KNIGHT, Color.BLACK));
        placePiece(new Position(0, 2), PieceFactory.createPiece(PieceType.BISHOP, Color.BLACK));
        placePiece(new Position(0, 3), PieceFactory.createPiece(PieceType.QUEEN, Color.BLACK));
        placePiece(new Position(0, 4), PieceFactory.createPiece(PieceType.KING, Color.BLACK));
        placePiece(new Position(0, 5), PieceFactory.createPiece(PieceType.BISHOP, Color.BLACK));
        placePiece(new Position(0, 6), PieceFactory.createPiece(PieceType.KNIGHT, Color.BLACK));
        placePiece(new Position(0, 7), PieceFactory.createPiece(PieceType.ROOK, Color.BLACK));
        
        for (int i = 0; i < 8; i++) {
            placePiece(new Position(1, i), PieceFactory.createPiece(PieceType.PAWN, Color.BLACK));
        }
    }
    
    public void placePiece(Position pos, Piece piece) {
        board[pos.getRow()][pos.getCol()] = piece;
        piecePositions.put(pos, piece);
    }
    
    public void removePiece(Position pos) {
        board[pos.getRow()][pos.getCol()] = null;
        piecePositions.remove(pos);
    }
    
    public Piece getPiece(Position pos) {
        return board[pos.getRow()][pos.getCol()];
    }
    
    public boolean isOccupied(Position pos) {
        return getPiece(pos) != null;
    }
    
    public boolean isOccupiedBySameColor(Position pos, Color color) {
        Piece piece = getPiece(pos);
        return piece != null && piece.getColor() == color;
    }
    
    public void movePiece(Position from, Position to) {
        Piece piece = getPiece(from);
        if (piece != null) {
            // Remove captured piece if any
            Piece capturedPiece = getPiece(to);
            if (capturedPiece != null) {
                piecePositions.remove(to);
            }
            
            // Move the piece
            board[from.getRow()][from.getCol()] = null;
            board[to.getRow()][to.getCol()] = piece;
            
            // Update piece positions map
            piecePositions.remove(from);
            piecePositions.put(to, piece);
            
            piece.setMoved(true);
        }
    }
    
    public Position findKing(Color color) {
        for (Map.Entry<Position, Piece> entry : piecePositions.entrySet()) {
            if (entry.getValue().getType() == PieceType.KING && entry.getValue().getColor() == color) {
                return entry.getKey();
            }
        }
        return new Position(-1, -1); // Invalid position if not found
    }
    
    public List<Position> getAllPiecesOfColor(Color color) {
        List<Position> pieces = new ArrayList<>();
        for (Map.Entry<Position, Piece> entry : piecePositions.entrySet()) {
            if (entry.getValue().getColor() == color) {
                pieces.add(entry.getKey());
            }
        }
        return pieces;
    }

    public void display() {
        final int cellW = 3;  // cell width

        // — horizontal border —
        Runnable printBorder = () -> {
            System.out.print("  +");
            for (int i = 0; i < 8; ++i)
                System.out.print("-".repeat(cellW) + "+");
            System.out.println();
        };

        // — top border —
        printBorder.run();

        // — column labels inside the grid —
        System.out.print("  |");
        for (char f = 'a'; f <= 'h'; ++f) {
            int pad = (cellW - 1) / 2;
            System.out.print(" ".repeat(pad) + f + " ".repeat(cellW - 1 - pad) + "|");
        }
        System.out.println();

        // — border under labels —
        printBorder.run();

        // — each rank of pieces —
        for (int rank = 8; rank >= 1; --rank) {
            int row = 8 - rank;
            System.out.print(rank + " |");

            for (int file = 0; file < 8; ++file) {
                Piece p = board[row][file];
                String s = p != null ? p.toString() : "  ";  // two spaces if empty

                // center a 2-char string in cellW
                int pad = (cellW - 2) / 2;
                System.out.print(" ".repeat(pad) + s + " ".repeat(cellW - 2 - pad) + "|");
            }

            System.out.println(" " + rank);
            printBorder.run();
        }

        // — bottom labels inside the grid —
        System.out.print("  |");
        for (char f = 'a'; f <= 'h'; ++f) {
            int pad = (cellW - 1) / 2;
            System.out.print(" ".repeat(pad) + f + " ".repeat(cellW - 1 - pad) + "|");
        }
        System.out.println();

        // — final border —
        printBorder.run();
    }
}

// Chess Rules interface - Strategy Pattern for game rules
interface ChessRules {
    boolean isValidMove(Move move, Board board);
    boolean isInCheck(Color color, Board board);
    boolean isCheckmate(Color color, Board board);
    boolean isStalemate(Color color, Board board);
    boolean wouldMoveCauseCheck(Move move, Board board, Color kingColor);
}

class StandardChessRules implements ChessRules {
    @Override
    public boolean isValidMove(Move move, Board board) {
        Piece piece = move.getPiece();
        List<Position> possibleMoves = piece.getPossibleMoves(move.getFrom(), board);
        
        // Check if the target position is in possible moves
        boolean validDestination = false;
        for (Position pos : possibleMoves) {
            if (pos.equals(move.getTo())) {
                validDestination = true;
                break;
            }
        }
        
        if (!validDestination) {
            return false;
        }
        
        // Check if move would put own king in check
        return !wouldMoveCauseCheck(move, board, piece.getColor());
    }
    
    @Override
    public boolean wouldMoveCauseCheck(Move move, Board board, Color kingColor) {
        // Create a temporary copy to simulate the move safely
        Piece movingPiece = board.getPiece(move.getFrom());
        Piece capturedPiece = board.getPiece(move.getTo());
        
        if (movingPiece == null) return true; // Invalid move
        
        // Temporarily execute the move
        board.removePiece(move.getFrom());
        if (capturedPiece != null) {
            board.removePiece(move.getTo());
        }
        board.placePiece(move.getTo(), movingPiece);
        
        // Check if king is in check after the move
        boolean inCheck = isInCheck(kingColor, board);
        
        // Undo the move
        board.removePiece(move.getTo());
        board.placePiece(move.getFrom(), movingPiece);
        if (capturedPiece != null) {
            board.placePiece(move.getTo(), capturedPiece);
        }
        
        return inCheck;
    }
    
    @Override
    public boolean isInCheck(Color color, Board board) {
        Position kingPos = board.findKing(color);
        if (kingPos.getRow() == -1) return false; // King not found
        
        Color opponentColor = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
        List<Position> opponentPieces = board.getAllPiecesOfColor(opponentColor);
        
        for (Position pos : opponentPieces) {
            Piece piece = board.getPiece(pos);
            List<Position> moves = piece.getPossibleMoves(pos, board);
            for (Position targetPos : moves) {
                if (targetPos.equals(kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean isCheckmate(Color color, Board board) {
        if (!isInCheck(color, board)) return false;
        
        List<Position> pieces = board.getAllPiecesOfColor(color);
        for (Position pos : pieces) {
            Piece piece = board.getPiece(pos);
            List<Position> moves = piece.getPossibleMoves(pos, board);
            
            for (Position targetPos : moves) {
                Move move = new Move(pos, targetPos, piece, board.getPiece(targetPos));
                if (isValidMove(move, board)) {
                    return false; // Found a valid move, not checkmate
                }
            }
        }
        return true;
    }
    
    @Override
    public boolean isStalemate(Color color, Board board) {
        if (isInCheck(color, board)) return false;
        
        List<Position> pieces = board.getAllPiecesOfColor(color);
        for (Position pos : pieces) {
            Piece piece = board.getPiece(pos);
            List<Position> moves = piece.getPossibleMoves(pos, board);
            
            for (Position targetPos : moves) {
                Move move = new Move(pos, targetPos, piece, board.getPiece(targetPos));
                if (isValidMove(move, board)) {
                    return false; // Found a valid move, not stalemate
                }
            }
        }
        return true;
    }
}

// Message class for chat functionality
class Message {
    private String senderId;
    private String content;
    private long timestamp;

    public Message(String sId, String msg) {
        senderId = sId;
        content = msg;
        timestamp = System.currentTimeMillis();
    }
    
    public String getSenderId() { 
        return senderId; 
    }
    public String getContent() { 
        return content; 
    }
    public long getTimestamp() { 
        return timestamp; 
    }
    
    @Override
    public String toString() {
        return "[" + senderId + "]: " + content;
    }
}

// Mediator Pattern - Interface
interface ChatMediator {
    void sendMessage(Message message, User user);
    void addUser(User user);
    void removeUser(User user);
}

// Colleague interface for Mediator Pattern
abstract class Colleague {
    protected ChatMediator mediator;
    
    public Colleague() { 
        mediator = null; 
    }
    public abstract void send(Message message);
    public abstract void receive(Message message);
    public void setMediator(ChatMediator med) { 
        mediator = med; 
    }
    public ChatMediator getMediator() { 
        return mediator; 
    }
}

// User class now inheriting from Colleague for proper Mediator Pattern
class User extends Colleague {
    private String id;
    private String name;
    private int score;

    public User(String userId, String userName) {
        super();
        id = userId;
        name = userName;
        score = 1000; // Starting score
    }
    
    public String getId() { 
        return id; 
    }
    public String getName() { 
        return name; 
    }
    public int getScore() { 
        return score; 
    }
    
    public void incrementScore(int points) {
        score += points;
    }
    
    public void decrementScore(int points) {
        score -= points;
    }
    
    @Override
    public String toString() {
        return name + " (Score: " + score + ")";
    }
    
    // Implement Colleague interface
    @Override
    public void send(Message message) {
        if (mediator != null) {
            mediator.sendMessage(message, this);
        }
    }
    
    @Override
    public void receive(Message message) {
        System.out.println("User " + name + " received message from " + message.getSenderId() + ": " + message.getContent());
    }
}

// Match class implementing Mediator Pattern
class Match implements ChatMediator {
    private String matchId;
    private User whitePlayer;
    private User blackPlayer;
    private Board board;
    private ChessRules rules;
    private Color currentTurn;
    private GameStatus status;
    private List<Move> moveHistory;
    private List<Message> chatHistory;

    public Match(String mId, User white, User black) {
        matchId = mId;
        whitePlayer = white;
        blackPlayer = black;
        board = new Board();
        rules = new StandardChessRules();
        currentTurn = Color.WHITE;
        status = GameStatus.IN_PROGRESS;
        moveHistory = new ArrayList<>();
        chatHistory = new ArrayList<>();
        
        // Set mediator for both users
        whitePlayer.setMediator(this);
        blackPlayer.setMediator(this);
        
        System.out.println("Match started between " + whitePlayer.getName() + " (White) and " 
             + blackPlayer.getName() + " (Black)");
    }
    
    public boolean makeMove(Position from, Position to, User player) {
        if (status != GameStatus.IN_PROGRESS) {
            System.out.println("Game is not in progress!");
            return false;
        }
        
        Color playerColor = getPlayerColor(player);
        if (playerColor != currentTurn) {
            System.out.println("It's not your turn!");
            return false;
        }
        
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != playerColor) {
            System.out.println("Invalid piece selection!");
            return false;
        }
        
        Move move = new Move(from, to, piece, board.getPiece(to));
        
        if (!rules.isValidMove(move, board)) {
            System.out.println("Invalid move!");
            return false;
        }
        
        // Execute move
        board.movePiece(from, to);
        moveHistory.add(move);
        
        System.out.println(player.getName() + " moved " + piece.getSymbol() 
             + " from " + from.toChessNotation() + " to " + to.toChessNotation());
        
        board.display();
        
        // Check game end conditions
        Color opponentColor = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        if (rules.isCheckmate(opponentColor, board)) {
            endGame(player, "checkmate");
            return true;
        } 
        else if (rules.isStalemate(opponentColor, board)) {
            endGame(player, "stalemate");
            return true;
        } 
        else {
            currentTurn = opponentColor;
            if (rules.isInCheck(opponentColor, board)) {
                System.out.println(getPlayerByColor(opponentColor).getName() + " is in check!");
            }
        }
        
        return true;
    }
    
    public void quitGame(User player) {
        User opponent = (player == whitePlayer) ? blackPlayer : whitePlayer;
        endGame(opponent, "quit");
        player.decrementScore(50); // Penalty for quitting
        System.out.println(player.getName() + " quit the game. Score decreased by 50.");
    }
    
    public void endGame(User winner, String reason) {
        status = GameStatus.COMPLETED;
        
        if (winner != null) {
            User loser = (winner == whitePlayer) ? blackPlayer : whitePlayer;
            winner.incrementScore(30);
            loser.decrementScore(20);
            System.out.println("Game ended - " + winner.getName() + " wins by " + reason + "!");
            System.out.println("Score update: " + winner.getName() + " +30, " + loser.getName() + " -20");
        } 
        else {
            System.out.println("Game ended in " + reason + "! No score change.");
        }
    }
    
    public Color getPlayerColor(User player) {
        return (player == whitePlayer) ? Color.WHITE : Color.BLACK;
    }
    
    public User getPlayerByColor(Color color) {
        return (color == Color.WHITE) ? whitePlayer : blackPlayer;
    }
    
    // Mediator Pattern implementation
    @Override
    public void sendMessage(Message message, User user) {
        chatHistory.add(message);
        
        User recipient = (user == whitePlayer) ? blackPlayer : whitePlayer;
        recipient.receive(message);
        System.out.println("Chat in match " + matchId + " - " + message.getContent());
    }
    
    @Override
    public void addUser(User user) {
        // Not applicable for chess match (always 2 users)
    }
    
    @Override
    public void removeUser(User user) {
        quitGame(user);
    }
    
    public String getMatchId() { 
        return matchId; 
    }
    public GameStatus getStatus() { 
        return status; 
    }
    public User getWhitePlayer() { 
        return whitePlayer; 
    }
    public User getBlackPlayer() { 
        return blackPlayer; 
    }
    public Board getBoard() { 
        return board; 
    }
}

// Matching Strategy interface
interface MatchingStrategy {
    User findMatch(User user, List<User> waitingUsers);
}

// Score-based matching strategy
class ScoreBasedMatching implements MatchingStrategy {
    private int scoreTolerance;

    public ScoreBasedMatching(int tolerance) {
        scoreTolerance = tolerance;
    }
    
    @Override
    public User findMatch(User user, List<User> waitingUsers) {
        User bestMatch = null;
        int bestScoreDiff = Integer.MAX_VALUE;
        
        for (User waitingUser : waitingUsers) {
            if (!waitingUser.getId().equals(user.getId())) {
                int scoreDiff = Math.abs(waitingUser.getScore() - user.getScore());
                if (scoreDiff <= scoreTolerance && scoreDiff < bestScoreDiff) {
                    bestMatch = waitingUser;
                    bestScoreDiff = scoreDiff;
                }
            }
        }
        return bestMatch;
    }
}

// Game Manager - Singleton Pattern
class GameManager {
    private static GameManager instance;
    private Map<String, Match> activeMatches; // matchId --> Match
    private List<User> waitingUsers;
    private MatchingStrategy matchingStrategy;
    private int matchCounter;
    
    private GameManager() {
        activeMatches = new HashMap<>();
        waitingUsers = new ArrayList<>();
        matchingStrategy = new ScoreBasedMatching(100); // 100 points tolerance
        matchCounter = 0;
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    public void requestMatch(User user) {
        System.out.println(user.getName() + " is looking for a match...");
        
        User opponent = matchingStrategy.findMatch(user, waitingUsers);
        
        if (opponent != null) {
            // Remove opponent from waiting list
            waitingUsers.remove(opponent);
            
            String matchId = "MATCH_" + (++matchCounter);
            Match match = new Match(matchId, user, opponent);
            activeMatches.put(matchId, match);
            
            System.out.println("Match found! " + user.getName() + " vs " + opponent.getName());
            match.getBoard().display();
        } 
        else {
            waitingUsers.add(user);
            System.out.println(user.getName() + " added to waiting list.");
        }
    }
    
    public void makeMove(String matchId, Position from, Position to, User player) {
        if (activeMatches.containsKey(matchId)) {
            Match match = activeMatches.get(matchId);
            match.makeMove(from, to, player);
            
            if (match.getStatus() == GameStatus.COMPLETED) {
                activeMatches.remove(matchId);
                System.out.println("Match " + matchId + " completed and removed from active matches.");
            }
        }
    }
    
    public void quitMatch(String matchId, User player) {
        if (activeMatches.containsKey(matchId)) {
            Match match = activeMatches.get(matchId);
            match.quitGame(player);
            activeMatches.remove(matchId);
        }
    }
    
    public void sendChatMessage(String matchId, String message, User user) {
        if (activeMatches.containsKey(matchId)) {
            Match match = activeMatches.get(matchId);
            Message msg = new Message(user.getId(), message);
            match.sendMessage(msg, user);
        }
    }
    
    public Match getMatch(String matchId) {
        if (activeMatches.containsKey(matchId)) {
            return activeMatches.get(matchId);
        }
        return null;
    }
    
    public void displayActiveMatches() {
        System.out.println("\n=== Active Matches ===");
        for (Map.Entry<String, Match> entry : activeMatches.entrySet()) {
            Match match = entry.getValue();
            System.out.println("Match " + match.getMatchId() + ": " 
                 + match.getWhitePlayer().getName() + " vs " 
                 + match.getBlackPlayer().getName());
        }
        System.out.println("Total active matches: " + activeMatches.size());
        System.out.println("Users waiting: " + waitingUsers.size());
    }
}

// Util class for basic demo
class ChessSystemDemo {
    // Method to demonstrate Scholar's Mate (4-move checkmate)
    public static void demonstrateScholarsMate() {
        System.out.println("\n=== Scholar's Mate Demo (4-move checkmate) ===");
        
        User aditya = new User("DEMO_1", "Aditya");
        User rohit = new User("DEMO_2", "Rohit");
        
        Match demoMatch = new Match("DEMO_MATCH", aditya, rohit);
        demoMatch.getBoard().display();
        
        // Proper Scholar's Mate sequence with correct coordinates
        System.out.println("\nMove 1: White e2-e4");
        demoMatch.makeMove(new Position(6, 4), new Position(4, 4), aditya); // e2-e4
        
        System.out.println("\nMove 1: Black e7-e5");
        demoMatch.makeMove(new Position(1, 4), new Position(3, 4), rohit); // e7-e5
        
        System.out.println("\nMove 2: White Bf1-c4 (targeting f7)");
        demoMatch.makeMove(new Position(7, 5), new Position(4, 2), aditya); // Bf1-c4
        
        System.out.println("\nMove 2: Black Nb8-c6 (developing)");
        demoMatch.makeMove(new Position(0, 1), new Position(2, 2), rohit); // Nb8-c6
        
        System.out.println("\nMove 3: White Qd1-h5 (attacking f7 and h7)");
        demoMatch.makeMove(new Position(7, 3), new Position(3, 7), aditya); // Qd1-h5 (row 3, col 7 = h5)
        
        System.out.println("\nMove 3: Black Ng8-f6?? (defending h7 but exposing f7)");
        demoMatch.makeMove(new Position(0, 6), new Position(2, 5), rohit); // Ng8-f6
        
        System.out.println("\nMove 4: White Qh5xf7# (Checkmate!)");
        boolean gameEnded = demoMatch.makeMove(new Position(3, 7), new Position(1, 5), aditya); // Qh5xf7#
        
        if (demoMatch.getStatus() != GameStatus.COMPLETED) {
            System.out.println("Note: Checkmate detection may need refinement for this position.");
        }
        
        // Demonstrate chat functionality
        System.out.println("\n=== Testing Chat Functionality ===");
        aditya.send(new Message(aditya.getId(), "Good game!"));
        rohit.send(new Message(rohit.getId(), "Thanks, that was a quick one!"));
    }
}

// Main class to run the chess system
public class Chess {
    public static void main(String[] args) {
        System.out.println("=== Chess System with Design Patterns Demo ===");
        
        // Test Scholar's Mate
        ChessSystemDemo.demonstrateScholarsMate();
        
        // Demonstrate Game Manager functionality
        System.out.println("\n=== Game Manager Demo ===");
        GameManager gm = GameManager.getInstance();
        
        User saurav = new User("USER_1", "Saurav");
        User manish = new User("USER_2", "Manish");
        User abhishek = new User("USER_3", "Abishek");
        
        System.out.println("\nUsers: " + saurav.toString() + ", " + manish.toString() + ", " + abhishek.toString());
        
        // Request matches
        gm.requestMatch(saurav);
        gm.requestMatch(manish);  // Should create a match
        gm.requestMatch(abhishek); // Should go to waiting list
        
        gm.displayActiveMatches();
    }
}