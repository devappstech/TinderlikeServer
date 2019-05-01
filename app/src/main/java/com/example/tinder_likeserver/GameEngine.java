package com.example.tinder_likeserver;

import java.util.Random;

public class GameEngine {
    private static final Random RANDOM = new Random();
    private int[][] board;
    private int currentPlayer;
    private boolean ended;

    public static final int NO_PLAYER = 0;
    public static final int PLAYER_X = 1;
    public static final int PLAYER_0 = 2;

    public GameEngine() {
        board = new int[3][3];
        newGame();
    }

    public boolean isEnded() {
        return ended;
    }

    public int play(int x, int y) {
        if (!ended  &&  board[x][y] == NO_PLAYER) {
            board[x][y] = currentPlayer;
            changePlayer();
        }
        return checkEnd();
    }

    public void changePlayer() {
        currentPlayer = (currentPlayer == PLAYER_X ? PLAYER_0 : PLAYER_X);
    }

    public int getElt(int x, int y) {
        return board[x][y];
    }

    public void newGame() {
        for (int i = 0; i  < 3; i++) {
            for(int j = 0; j < 3; j ++){
                board[i][j] = NO_PLAYER;
            }
        }

        currentPlayer = PLAYER_0;
        ended = false;
    }

    public int checkEnd() {
        for (int i = 0; i < 3; i++) {
            //checking horizontally
            if (board[i][0] != NO_PLAYER && board[i][0] == board[i][1] && board[i][0] == board[i][2]) {
                ended = true;
                return board[i][0];
            }
            //checking vertically
            if (board[0][i] != NO_PLAYER && board[0][i] == board[1][i] && board[0][i] == board[2][i]) {
                ended = true;
                return board[0][i];
            }
        }

        //checking diagonally
        if (board[0][0] != NO_PLAYER && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            ended = true;
            return board[0][0];
        }
        if (board[2][0] != NO_PLAYER && board[2][0] == board[1][1] && board[1][1] == board[0][2]) {
            ended = true;
            return board[2][0];
        }

        //checking for other cells
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == NO_PLAYER)
                    return NO_PLAYER;
            }
        }

        return -1;
    }
}

