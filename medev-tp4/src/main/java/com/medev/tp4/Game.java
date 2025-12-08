/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.medev.tp4;

/**
 *
 * @author user
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game implements Serializable {

    private Board board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    boolean run =true ;

    public Game() {
        board = new Board();
        player1 = new Player(Color.WHITE);
        player2 = new Player(Color.BLACK);
        currentPlayer = player1;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (run) {
            board.display();
            System.out.println("Current player: " + currentPlayer.getColor());
            
            List<Move> captures = getPossibleCaptures();
            if (!captures.isEmpty()) {
                System.out.println("Capture is mandatory!");
            }
            
            System.out.println("Enter your move (fromRow fromCol toRow toCol), 'save' to save, 'load' to load , or 'quit' to quit:");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("save")) {
                saveGame("dame.txt");
                continue;
            } else if (input.equalsIgnoreCase("load")) {
                loadGame("dame.txt");
                continue;
            } else if (input.equalsIgnoreCase("quit")){
                System.out.println("Game ended");
                run = false ;
                continue;
            }

            try {
                String[] parts = input.split(" ");
                int fromRow = Integer.parseInt(parts[0]);
                int fromCol = Integer.parseInt(parts[1]);
                int toRow = Integer.parseInt(parts[2]);
                int toCol = Integer.parseInt(parts[3]);

                Move move = new Move(fromRow, fromCol, toRow, toCol);
                if (isValidMove(move)) {
                    
                    boolean isCapture = Math.abs(move.getFromRow() - move.getToRow()) == 2;

                    board.movePiece(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
                    
                    if (isCapture) {
                        int capturedRow = (move.getFromRow() + move.getToRow()) / 2;
                        int capturedCol = (move.getFromCol() + move.getToCol()) / 2;
                        board.removePiece(capturedRow, capturedCol);
                    }
                    
                    promotePawn(move.getToRow(), move.getToCol());
                    
                    if (isCapture) {
                        List<Move> nextCaptures = getPossibleCaptures(move.getToRow(), move.getToCol());
                        if (!nextCaptures.isEmpty()) {
                            // Don't switch player, let them play again
                            continue;
                        }
                    }

                    switchPlayer();
                } else {
                    System.out.println("Invalid move. Try again.");
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.out.println("Invalid input. Please use the format 'fromRow fromCol toRow toCol'.");
            }
        }
    }

    private boolean isValidMove(Move move) {
        List<Move> possibleMoves = getPossibleMoves(move.getFromRow(), move.getFromCol());
        for (Move possibleMove : possibleMoves) {
            if (possibleMove.getToRow() == move.getToRow() && possibleMove.getToCol() == move.getToCol()) {
                return true;
            }
        }
        return false;
    }
    
    private List<Move> getPossibleMoves(int row, int col) {
        List<Move> captures = getPossibleCaptures(row, col);
        if (!captures.isEmpty()) {
            return captures;
        }

        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(row, col);
        if (piece == null || piece.getColor() != currentPlayer.getColor()) {
            return moves;
        }

        if (piece.isQueen()) {
            // Queen moves
            int[] directions = {-1, 1};
            for (int dRow : directions) {
                for (int dCol : directions) {
                    for (int i = 1; i < board.getSize(); i++) {
                        int newRow = row + i * dRow;
                        int newCol = col + i * dCol;
                        if (newRow < 0 || newRow >= board.getSize() || newCol < 0 || newCol >= board.getSize()) {
                            break;
                        }
                        if (board.getPiece(newRow, newCol) == null) {
                            moves.add(new Move(row, col, newRow, newCol));
                        } else {
                            break;
                        }
                    }
                }
            }
        } else {
            // Pawn moves
            int direction = (piece.getColor() == Color.WHITE) ? -1 : 1;
            int[] dCols = {-1, 1};

            // Regular moves
            for (int dCol : dCols) {
                int newRow = row + direction;
                int newCol = col + dCol;
                if (newRow >= 0 && newRow < board.getSize() && newCol >= 0 && newCol < board.getSize() && board.getPiece(newRow, newCol) == null) {
                    moves.add(new Move(row, col, newRow, newCol));
                }
            }
        }
        return moves;
    }
    
    private List<Move> getPossibleCaptures() {
        List<Move> captures = new ArrayList<>();
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                captures.addAll(getPossibleCaptures(row, col));
            }
        }
        return captures;
    }

    
    private List<Move> getPossibleCaptures(int row, int col) {
        List<Move> captures = new ArrayList<>();
        Piece piece = board.getPiece(row, col);
        if (piece == null || piece.getColor() != currentPlayer.getColor()) {
            return captures;
        }

        if (piece.isQueen()) {
            // Queen captures
            int[] directions = {-1, 1};
            for (int dRow : directions) {
                for (int dCol : directions) {
                    for (int i = 1; i < board.getSize(); i++) {
                        int newRow = row + i * dRow;
                        int newCol = col + i * dCol;
                        if (newRow < 0 || newRow >= board.getSize() || newCol < 0 || newCol >= board.getSize()) {
                            break;
                        }
                        if (board.getPiece(newRow, newCol) != null) {
                            if (board.getPiece(newRow, newCol).getColor() != currentPlayer.getColor()) {
                                int jumpRow = newRow + dRow;
                                int jumpCol = newCol + dCol;
                                if (jumpRow >= 0 && jumpRow < board.getSize() && jumpCol >= 0 && jumpCol < board.getSize() && board.getPiece(jumpRow, jumpCol) == null) {
                                    captures.add(new Move(row, col, jumpRow, jumpCol));
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            // Pawn captures
            int direction = (piece.getColor() == Color.WHITE) ? -1 : 1;
            int[] dCols = {-1, 1};

            for (int dCol : dCols) {
                int newRow = row + 2 * direction;
                int newCol = col + 2 * dCol;
                int capturedRow = row + direction;
                int capturedCol = col + dCol;
                if (newRow >= 0 && newRow < board.getSize() && newCol >= 0 && newCol < board.getSize() &&
                    board.getPiece(newRow, newCol) == null &&
                    board.getPiece(capturedRow, capturedCol) != null &&
                    board.getPiece(capturedRow, capturedCol).getColor() != currentPlayer.getColor()) {
                    captures.add(new Move(row, col, newRow, newCol));
                }
            }
        }
        return captures;
    }


    private void promotePawn(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece != null && !piece.isQueen()) {
            if (piece.getColor() == Color.WHITE && row == 0) {
                piece.promote();
            } else if (piece.getColor() == Color.BLACK && row == board.getSize() - 1) {
                piece.promote();
            }
        }
    }


    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private void saveGame(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("Game saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }

    private void loadGame(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Game loadedGame = (Game) ois.readObject();
            this.board = loadedGame.board;
            this.player1 = loadedGame.player1;
            this.player2 = loadedGame.player2;
            this.currentPlayer = loadedGame.currentPlayer;
            System.out.println("Game loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading game: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}

