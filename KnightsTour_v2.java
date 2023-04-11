import java.util.Arrays;
import java.util.Random;

public class KnightsTour_v2 {
    // Knight Moves
    private static final int[] MOVE_ROW_OFFSETS = {-2, -1, +1, +2, +2, +1, -1, -2};
    private static final int[] MOVE_COLUMN_OFFSETS = {+1, +2, +2, +1, -1, -2, -2, -1};
    private static final int NNE = 0, ENE = 1, ESE = 2, SSE = 3, SSW = 4, WSW = 5, WNW = 6, NNW = 7;  // list of possible moves: name = index
    private static final int[] MOVES = {NNE, ENE, ESE, SSE, SSW, WSW, WNW, NNW};

    // board size
    private static final int ROWS = 8;
    private static final int COLUMNS = 8;

    // other constants
    private static final int UNVISITED = 0;

    // (mutable) board state
    private static int[][] board;
    private static int knightRow, knightColumn;
    static int moveCount;

    // (mutable) Random algorithm state
    private static Random rng = new Random();

    // (mutable) Accessibility algorithm state
    private static int[][] accessibilityMatrix = null;


    private static void moveTo(int destinationRow, int destinationColumn) {
        knightRow = destinationRow;
        knightColumn = destinationColumn;
        board[knightRow][knightColumn] = ++moveCount;
    }


    private static boolean contains(int row, int column) {
        return (0 <= row && row < ROWS) && (0 <= column && column < COLUMNS);
    }


    private static boolean isValidPosition(int row, int column) {
        return contains(row, column) && board[row][column] == UNVISITED;
    }


    private static boolean isValidMove(int move) {
        return isValidPosition(knightRow + MOVE_ROW_OFFSETS[move], knightColumn + MOVE_COLUMN_OFFSETS[move]);
    }

    private static boolean move(int move) {
        int destinationRow = knightRow + MOVE_ROW_OFFSETS[move];
        int destinationColumn = knightColumn + MOVE_COLUMN_OFFSETS[move];
        boolean valid = isValidPosition(destinationRow, destinationColumn);
        if (valid)
            moveTo(destinationRow, destinationColumn);
        return valid;
    }


    static boolean isComplete() {
        return moveCount == ROWS * COLUMNS;
    }

    private static String tourToString() {
        StringBuilder str = new StringBuilder();
        for (int[] row : board)
            str.append(Arrays.toString(row)).append('\n');
        return str.toString();
    }


    private static int decideRandom() {
        // filter out all invalid moves, so only valid moves are in the `validMoves` array
        int[] validMoves = new int[MOVES.length];
        int n = 0;  // number of valid moves
        for (int move : MOVES)
            if (isValidMove(move))
                validMoves[n++] = move;  // add to valid moves array, and increment `n`

        if (n == 0)
            return -1;  // there are no more valid moves from this position
        return validMoves[rng.nextInt(n)];  // return a random valid move
    }

    static void runRandomAlgorithm(int startRow, int startColumn) {
        // initialize state
        board = new int[ROWS][COLUMNS];
        knightRow = startRow;
        knightColumn = startColumn;
        moveCount = 0;

        // loop
        while (!isComplete()) {
            // use the given algorithm to decide on the next move to make
            int move = decideRandom();
            if (move < 0)  // was there a move available?
                break;
            move(move);
        }
    }

    private static void initializeAccessibilityAlgorithm() {
        accessibilityMatrix = new int[ROWS][COLUMNS];

        for (int row = 0; row < ROWS; row++)
            for (int column = 0; column < COLUMNS; column++) {  // for each position on the board

                // Count the number of valid knight moves from this posistion.
                // (Which is the same as the number of valid knight moves to this
                // position, because all knight moves are invertable.)
                int count = 0;
                for (int move : MOVES)
                    if (isValidPosition(row + MOVE_ROW_OFFSETS[move], column + MOVE_COLUMN_OFFSETS[move]))
                        count++;

                // update accessibility matrix
                accessibilityMatrix[row][column] = count;
            }
    }


    private static int decideAccessible() {
        // Use low-water-mark algorithm to find the (valid) move
        // with the lowest accessibility socre.
        int bestMove = -1;  // i.e. least accessible valid move
        int bestScore = Integer.MAX_VALUE;  // accessibilty score of `bestMove`
        for (int move : MOVES) {
            int destinationRow = knightRow + MOVE_ROW_OFFSETS[move];
            int destinationColumn = knightColumn + MOVE_COLUMN_OFFSETS[move];
            if (!isValidPosition(destinationRow, destinationColumn))
                continue;  // we ignore invalid moves from consideration

            // accessibilty score for the given move
            int score = accessibilityMatrix[destinationRow][destinationColumn];

            // have we found a better (i.e. more accessible) move?
            if (score < bestScore) {
                // then update the `bestMove` and its associated `bestScore`.
                bestMove = move;
                bestScore = score;
            }
        }

        if (bestMove >= 0) {
            // Now we need to update the accessibiltyMatirx to reflect this new move.
            // All the "adjacent" squares have one less valid way to get to them
            // since this square is no longer valid. (It has been visited.)
            int newKnightRow = knightRow + MOVE_ROW_OFFSETS[bestMove];  // new knight position
            int newKnightColumn = knightColumn + MOVE_COLUMN_OFFSETS[bestMove];
            accessibilityMatrix[newKnightRow][newKnightColumn] = 0;
            for (int move : MOVES) {
                int destinationRow = newKnightRow + MOVE_ROW_OFFSETS[move];
                int destinationColumn = newKnightColumn + MOVE_COLUMN_OFFSETS[move];
                if (contains(destinationRow, destinationColumn))
                    accessibilityMatrix[destinationRow][destinationColumn]--;
            }
        }

        // Done.
        return bestMove;
    }


    private static void runAccessibilityAlgorithm(int startRow, int startColumn) {
        // initialize state
        board = new int[ROWS][COLUMNS];
        knightRow = startRow;
        knightColumn = startColumn;
        moveCount = 0;

        // initialize
        initializeAccessibilityAlgorithm();

        // loop
        while (!isComplete()) {
            // use the given algorithm to decide on the next move to make
            int move = decideAccessible();
            if (move < 0)  // was there a move available?
                break;
            move(move);
        }
    }
    public static void main(String[] args) {
        {	// Random algorithm
            System.out.println("-- Random Algorithm: --");
            runRandomAlgorithm(0, 0);
            System.out.println("Move Count: " + moveCount);
            System.out.println("Tour Completed: " + isComplete());
            System.out.println(tourToString());
            System.out.println();
        }

        {	//  Random algorithms
            final int N = 1000;
            System.out.println("-- " + N + " Random Algorithms: --");
            int count = 0;  // successful completion count
            int sum = 0;  // sum of move counts to calculate an average
            int max = 0;  // maximum move count achieved
            int[][] bestBoard = null;  // store best tour
            int bestKnightRow = -1, bestKnightColumn = -1;
            for (int i = 0; i < N; i++) {
                runRandomAlgorithm(rng.nextInt(ROWS), rng.nextInt(COLUMNS));
                if (isComplete())
                    count++;
                sum += moveCount;
                if (moveCount > max) {
                    max = moveCount;
                    bestBoard = board;
                    bestKnightRow = knightRow;
                    bestKnightColumn = knightColumn;
                }
            }
            System.out.printf("Completion Percentage: %.1f%%%n", (double) count / N);
            System.out.println("Max move count: " + max);
            System.out.printf("Average move count: %.2f%n", (double) sum / N);
            System.out.println("Best tour:");
            board = bestBoard;
            knightRow = bestKnightRow;
            knightColumn = bestKnightColumn;
            System.out.println(tourToString());
            System.out.println();
        }

        {	// Accessibilty Heuristic
            System.out.println("-- Accessibilty Heuristic: --");
            int count = 0;  // number of tours that completed successfully
            for (int row = 0; row < ROWS; row++)
                for (int column = 0; column < COLUMNS; column++) {
                    runAccessibilityAlgorithm(row, column);
                    if (isComplete())
                        count++;
                    else
                        System.out.println("Tour failed when starting from row=" + row + ",column=" + column);
                }
            System.out.println("Completed tours: " + count);
            System.out.println();
        }

        {	// Brute-Force Random Algorithm
            final int MAX_N = 10000000;  // limit: so program doesn't run forever
            final long seed = System.currentTimeMillis() + System.nanoTime();
            System.out.println("-- Brute-Force Random Algorithms: --");
            int sum = 0;  // sum of move counts to calculate an average
            int max = 0;  // maximum move count achieved
            int[][] bestBoard = null;  // store best tour

            int i;
            for (i = 0; i < MAX_N; ) {
                i++;

                Random rng = new Random(seed + i);
                runRandomAlgorithm(rng.nextInt(ROWS), rng.nextInt(COLUMNS));
                sum += moveCount;
                if (moveCount > max) {
                    System.out.println("working (" + max + ")...");
                    max = moveCount;
                    bestBoard = board;
                }

                if (isComplete())
                    break;
            }
            System.out.println("Iterations: " + i);
            System.out.printf("Average move count: %.2f%n", (double) sum / i);
            System.out.println("Best tour:");
            board = bestBoard;
            System.out.println(tourToString());
            System.out.println();

        }
    }

}