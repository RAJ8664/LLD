import java.util.*;

// Observer Pattern - for future notification service
interface IObserver {
    void update(String msg);
}

// Sample observer implementation
class ConsoleNotifier implements IObserver {
    public void update(String msg) {
        System.out.println("[Notification] " + msg);
    }
}

// Symbol/Mark class
class Symbol {
    private char mark;
    
    public Symbol(char m) {
        mark = m;
    }
    
    public char getMark() {
        return mark;
    }
}

// Board class - Dumb object that only manages the grid
class Board {
    private Symbol[][] grid;
    private int size;
    private Symbol emptyCell;
    
    public Board(int s) {
        size = s;
        emptyCell = new Symbol('-');
        grid = new Symbol[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                grid[i][j] = emptyCell;
            }
        }
    }
    
    public boolean isCellEmpty(int row, int col) {
        if(row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        return grid[row][col] == emptyCell;
    }
    
    public boolean placeMark(int row, int col, Symbol mark) {
        if(row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        if(!isCellEmpty(row, col)) {
            return false;
        }
        grid[row][col] = mark;
        return true;
    }
    
    public Symbol getCell(int row, int col) {
        if(row < 0 || row >= size || col < 0 || col >= size) {
            return emptyCell;
        }
        return grid[row][col];
    }
    
    public int getSize() {
        return size;
    }
    
    public Symbol getEmptyCell() {
        return emptyCell;
    }
    
    public void display() {
        System.out.print("\n  ");
        for(int i = 0; i < size; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        
        for(int i = 0; i < size; i++) {
            System.out.print(i + " ");
            for(int j = 0; j < size; j++) {
                System.out.print(grid[i][j].getMark() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}

// Player class --> 
class TicTacToePlayer {
    private int playerId;
    private String name;
    private Symbol symbol;
    private int score;
    
    public TicTacToePlayer(int playerId, String n, Symbol s) {
        this.playerId = playerId;
        name = n;
        symbol = s;
        score = 0;
    }
    
    // Getters and setters
    public String getName() { 
        return name; 
    }

    public Symbol getSymbol() { 
        return symbol; 
    }

    public int getScore() { 
        return score; 
    }

    public void incrementScore() { 
        score++;
    }
}

// Strategy Pattern for game rules
interface TicTacToeRules {
    boolean isValidMove(Board board, int row, int col);
    boolean checkWinCondition(Board board, Symbol symbol);
    boolean checkDrawCondition(Board board);
}

// Standard Tic Tac Toe rules
class StandardTicTacToeRules implements TicTacToeRules {
    public boolean isValidMove(Board board, int row, int col) {
        return board.isCellEmpty(row, col);
    }
    
    public boolean checkWinCondition(Board board, Symbol symbol) {
        int size = board.getSize();
        
        // Check rows
        for(int i = 0; i < size; i++) {
            boolean win = true;
            for(int j = 0; j < size; j++) {
                if(board.getCell(i, j) != symbol) {
                    win = false;
                    break;
                }
            }
            if(win) return true;
        }
        
        // Check columns
        for(int j = 0; j < size; j++) {
            boolean win = true;
            for(int i = 0; i < size; i++) {
                if(board.getCell(i, j) != symbol) {
                    win = false;
                    break;
                }
            }
            if(win) return true;
        }
        
        // Check main diagonal
        boolean win = true;
        for(int i = 0; i < size; i++) {
            if(board.getCell(i, i) != symbol) {
                win = false;
                break;
            }
        }
        if(win) return true;
        
        // Check anti-diagonal
        win = true;
        for(int i = 0; i < size; i++) {
            if(board.getCell(i, size-1-i) != symbol) {
                win = false;
                break;
            }
        }
        return win;
    }
    
    // If all cells are filled and no winner
    public boolean checkDrawCondition(Board board) {
        int size = board.getSize();
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                if(board.getCell(i, j) == board.getEmptyCell()) {
                    return false;
                }
            }
        }
        return true;
    }
}

// Game class --> Observable
class TicTacToeGame {
    private Board board;
    private Deque<TicTacToePlayer> players;
    private TicTacToeRules rules;
    private List<IObserver> observers;
    private boolean gameOver;
    
    public TicTacToeGame(int boardSize) {
        board = new Board(boardSize);
        players = new ArrayDeque<>();
        rules = new StandardTicTacToeRules();
        observers = new ArrayList<>();
        gameOver = false;
    }
    
    public void addPlayer(TicTacToePlayer player) {
        players.addLast(player);
    }
    
    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    public void notify(String msg) {
        for(IObserver observer : observers) {
            observer.update(msg);
        }
    }
    
    public void play() {
        if(players.size() < 2) {
            System.out.println("Need at least 2 players!");
            return;
        }
        
        notify("Tic Tac Toe Game Started!");
        
        Scanner scanner = new Scanner(System.in);
        
        while(!gameOver) {
            board.display();
            
            // Take out the current player from dequeue
            TicTacToePlayer currentPlayer = players.peekFirst();
            System.out.print(currentPlayer.getName() + " (" + currentPlayer.getSymbol().getMark() + ") - Enter row and column: ");
            
            int row = scanner.nextInt();
            int col = scanner.nextInt();
            
            // check if move is valid
            if(rules.isValidMove(board, row, col)) {
                board.placeMark(row, col, currentPlayer.getSymbol());
                notify(currentPlayer.getName() + " played (" + row + "," + col + ")");
                
                if(rules.checkWinCondition(board, currentPlayer.getSymbol())) {
                    board.display();
                    System.out.println(currentPlayer.getName() + " wins!");
                    currentPlayer.incrementScore();

                    notify(currentPlayer.getName() + " wins!");

                    gameOver = true;
                }
                else if(rules.checkDrawCondition(board)) {
                    board.display();
                    
                    System.out.println("It's a draw!");
                    notify("Game is Draw!");

                    gameOver = true;
                }
                else {
                    // Move player to back of queue
                    players.removeFirst();
                    players.addLast(currentPlayer);
                }
            }
            else {
                System.out.println("Invalid move! Try again.");
            }
        }
    }
}

// Enum & Factory Pattern for game creation
enum GameType {
    STANDARD
}

class TicTacToeGameFactory {
    public static TicTacToeGame createGame(GameType gt, int boardSize) {
        if(GameType.STANDARD == gt) {
            return new TicTacToeGame(boardSize);
        }
        return null;
    }
}

// Main class for Tic Tac Toe
public class TicTacToeMain {
    public static void main(String[] args) {
        System.out.println("=== TIC TAC TOE GAME ===");
        
        // Create game with custom board size
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter board size (e.g., 3 for 3x3): ");
        int boardSize = scanner.nextInt();
        
        TicTacToeGame game = TicTacToeGameFactory.createGame(GameType.STANDARD, boardSize);
        
        // Add observer
        IObserver notifier = new ConsoleNotifier();
        game.addObserver(notifier);
        
        // Create players with custom symbols
        TicTacToePlayer player1 = new TicTacToePlayer(1, "Aditya", new Symbol('X'));
        TicTacToePlayer player2 = new TicTacToePlayer(2, "Harshita", new Symbol('O'));
        
        game.addPlayer(player1);
        game.addPlayer(player2);
        
        // Play the game
        game.play();
        
        scanner.close();
    }
}