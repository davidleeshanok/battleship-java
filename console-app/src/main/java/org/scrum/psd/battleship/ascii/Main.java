package org.scrum.psd.battleship.ascii;

import org.scrum.psd.battleship.controller.GameController;
import org.scrum.psd.battleship.controller.dto.Letter;
import org.scrum.psd.battleship.controller.dto.Position;
import org.scrum.psd.battleship.controller.dto.Ship;

import java.util.*;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class Main {
    private static List<Ship> myFleet;
    private static List<Ship> enemyFleet;

    // probably make this a hash of configs later but for now we just need these 2 settings
    private static int boardWidth;
    private static int boardHeight;

    private static final Set<Position> myGuessedPositions = new HashSet<>();
    private static final Set<Position> enemyGuessedPositions = new HashSet<>();

    private static final Telemetry telemetry = new Telemetry();

    private static final com.diogonunes.jcolor.Attribute COLOR_SHIP = com.diogonunes.jcolor.Attribute.MAGENTA_TEXT();
    //private static final com.diogonunes.jcolor.Attribute COLOR_MESSAGES = com.diogonunes.jcolor.Attribute.WHITE_TEXT();
    private static final com.diogonunes.jcolor.Attribute COLOR_HITS = com.diogonunes.jcolor.Attribute.RED_TEXT();
    private static final com.diogonunes.jcolor.Attribute COLOR_MISSES = com.diogonunes.jcolor.Attribute.BLUE_TEXT();
    private static final com.diogonunes.jcolor.Attribute COLOR_INSTRUCTIONS = com.diogonunes.jcolor.Attribute.CYAN_TEXT();
    private static final com.diogonunes.jcolor.Attribute COLOR_INSTRUCTIONS_BG = com.diogonunes.jcolor.Attribute.WHITE_BACK();
    public static void main(String[] args) {
        telemetry.trackEvent("ApplicationStarted", "Technology", "Java");
        System.out.println(colorize("                                     |__", COLOR_SHIP));
        System.out.println(colorize("                                     |\\/", COLOR_SHIP));
        System.out.println(colorize("                                     ---", COLOR_SHIP));
        System.out.println(colorize("                                     / | [", COLOR_SHIP));
        System.out.println(colorize("                              !      | |||", COLOR_SHIP));
        System.out.println(colorize("                            _/|     _/|-++'", COLOR_SHIP));
        System.out.println(colorize("                        +  +--|    |--|--|_ |-", COLOR_SHIP));
        System.out.println(colorize("                     { /|__|  |/\\__|  |--- |||__/", COLOR_SHIP));
        System.out.println(colorize("                    +---------------___[}-_===_.'____                 /\\", COLOR_SHIP));
        System.out.println(colorize("                ____`-' ||___-{]_| _[}-  |     |_[___\\==--            \\/   _", COLOR_SHIP));
        System.out.println(colorize(" __..._____--==/___]_|__|_____________________________[___\\==--____,------' .7", COLOR_SHIP));
        System.out.println(colorize("|                        Welcome to Battleship                         BB-61/", COLOR_SHIP));
        System.out.println(colorize(" \\_________________________________________________________________________|", COLOR_SHIP));
        System.out.println("");

        InitializeGame();

        StartGame();
    }

    private static void StartGame() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("\033[2J\033[;H");
        System.out.println(colorize("GAME STARTING", COLOR_INSTRUCTIONS, COLOR_INSTRUCTIONS_BG));
        System.out.println(colorize("Each player (starting with player 1) will be prompted to select an enemy position to attack, repeating until one player has sunk all their opponent's ships", CYAN_TEXT(), WHITE_BACK()));
        System.out.println("                  __");
        System.out.println("                 /  \\");
        System.out.println("           .-.  |    |");
        System.out.println("   *    _.-'  \\  \\__/");
        System.out.println("    \\.-'       \\");
        System.out.println("   /          _/");
        System.out.println("  |      _  /\" \"");
        System.out.println("  |     /_\'");
        System.out.println("   \\    \\_/");
        System.out.println("    \" \"\" \"\" \"\" \"");

        do {
            Position position = new Position();
            boolean positionValid = false;
            while( !positionValid ) {
                System.out.println("");
                System.out.println(">>> Player, it's your turn");
                System.out.println("Enter coordinates for your shot :");
                position = parsePosition(scanner.next());
                positionValid = attackPositionValid(position);
                if( !positionValid ) {
                    System.out.println(colorize("Attack position is out of bounds, please try again", RED_TEXT()));
                }
            };

            myGuessedPositions.add(position);
            Optional<Ship> enemyShipHit = GameController.processShot(enemyFleet, position, myGuessedPositions);

            if (enemyShipHit.isPresent()) {
                System.out.println(colorize("Yeah ! Nice hit !",GREEN_TEXT()));

                Ship ship = enemyShipHit.get();
                if (ship.isSunk()) {
                    System.out.println(colorize("Success! You sunk the enemy's " + ship.getName(), GREEN_TEXT()));
                    printExplosion();
                    checkVictoryCondition();
                }
            }
            else {
                System.out.println(colorize("MISS",COLOR_MISSES));
            }

            System.out.println("");
            System.out.println(colorize("Enemy fleet status", BRIGHT_RED_TEXT()));
            printFleetStatus(enemyFleet);

            telemetry.trackEvent("Player_ShootPosition", "Position", position.toString(), "IsHit", Boolean.valueOf(isHit).toString());

            position = getRandomPosition();
            enemyGuessedPositions.add(position);

            System.out.println("");
            System.out.println("<<< Computer, it's your turn");
            telemetry.trackEvent("Computer_ShootPosition", "Position", position.toString(), "IsHit", Boolean.valueOf(isHit).toString());
            if (isHit) {
                System.out.println(colorize(String.format("Computer shoot in %s%s and %s", position.getColumn(), position.getRow(), "hit your ship !"),COLOR_HITS));
                beep();
                //System.out.println(colorize("Yeah ! Nice hit !",GREEN_TEXT()));
                System.out.println(colorize("                \\         .  ./",COLOR_HITS));
                System.out.println(colorize("              \\      .:\" \";'.:..\" \"   /",COLOR_HITS));
                System.out.println(colorize("                  (M^^.^~~:.'\" \").",COLOR_HITS));
                System.out.println(colorize("            -   (/  .    . . \\ \\)  -",COLOR_HITS));
                System.out.println(colorize("               ((| :. ~ ^  :. .|))",COLOR_HITS));
                System.out.println(colorize("            -   (\\- |  \\ /  |  /)  -",COLOR_HITS));
                System.out.println(colorize("                 -\\  \\     /  /-",COLOR_HITS));
                System.out.println(colorize("                   \\  \\   /  /",COLOR_HITS));
                checkVictoryCondition();
            } else {
                System.out.println(colorize(String.format("Computer shoots in %s%s and %s", position.getColumn(), position.getRow(), "missed"), COLOR_MISSES));
            }

            System.out.println("");
            System.out.println(colorize("Player fleet status", BRIGHT_GREEN_TEXT()));
            printFleetStatus(myFleet);
            
            // Clear screen here so it doesn't overwrite first turn instructions
            System.out.println("");
            pressEnterKeyToContinue();
            System.out.print("\033[2J\033[;H");

        } while (true);
    }

    private static boolean attackPositionValid(Position p) {
        if( p.getColumn() == null ) {
            return false;
        }
        if( p.getRow() < 1 || p.getRow() > boardWidth ) {
            return false;
        }

        return true;
    }

    private static void checkVictoryCondition() {
        boolean isMyFleetSunk = myFleet.stream().allMatch(Ship::isSunk);
        if (isMyFleetSunk) {
            System.out.println(colorize("Your fleet has been sunk! YOU LOSE", RED_TEXT()));
            System.exit(0);
        }

        boolean isEnemyFleetSunk = enemyFleet.stream().allMatch(Ship::isSunk);
        if (isEnemyFleetSunk) {
            System.out.println(colorize("You have sunk the enemy fleet! Congratulations! You have won Battleship!", GREEN_TEXT()));
            System.exit(0);
        }
    }

    private static void printExplosion() {
        System.out.print("\007");
        System.out.println(colorize("                \\         .  ./",COLOR_HITS));
        System.out.println(colorize("              \\      .:\" \";'.:..\" \"   /",COLOR_HITS));
        System.out.println(colorize("                  (M^^.^~~:.'\" \").",COLOR_HITS));
        System.out.println(colorize("            -   (/  .    . . \\ \\)  -",COLOR_HITS));
        System.out.println(colorize("               ((| :. ~ ^  :. .|))",COLOR_HITS));
        System.out.println(colorize("            -   (\\- |  \\ /  |  /)  -",COLOR_HITS));
        System.out.println(colorize("                 -\\  \\     /  /-",COLOR_HITS));
        System.out.println(colorize("                   \\  \\   /  /",COLOR_HITS));
    }

    private static void pressEnterKeyToContinue()
    { 
            System.out.println("Press Enter key to continue to the next round...");
            Scanner s = new Scanner(System.in);
            s.nextLine();
    }

    private static void printFleetStatus(List<Ship> fleet) {
        fleet.forEach(ship -> {
            System.out.print(ship.getName() + ": " );
            String sunk = ship.isSunk() ? "Sunk" : "Unsunken";
            System.out.println(colorize(sunk, ship.isSunk() ? RED_TEXT() : GREEN_TEXT()));
        });
    }

    protected static Position parsePosition(String input) {
        Letter letter = Letter.valueOf(input.toUpperCase().substring(0, 1));
        int number = Integer.parseInt(input.substring(1));
        return new Position(letter, number);
    }

    private static Position getRandomPosition() {
        Random random = new Random();
        Letter letter = Letter.values()[random.nextInt(boardHeight)];
        int number = random.nextInt(1, boardWidth + 1);
        Position position = new Position(letter, number);
        return position;
    }

    private static void InitializeGame() {
        // default to original board size until configurable by the user
        boardWidth = boardHeight = 10;

        InitializeMyFleet();
        InitializeEnemyFleet();
    }

    private static void InitializeMyFleet() {
        Scanner scanner = new Scanner(System.in);
        myFleet = GameController.initializeShips();

        System.out.println(colorize("SETUP PHASE", COLOR_INSTRUCTIONS, COLOR_INSTRUCTIONS_BG));
        System.out.println("");
        System.out.println(colorize("You will be prompted to enter the positions of each ship one at a time.", CYAN_TEXT(), WHITE_BACK()));
        System.out.println(colorize(String.format("Game board has size from %s to %s and 1 to %s.", Letter.values()[0], Letter.values()[Letter.values().length-1], boardWidth), CYAN_TEXT(), WHITE_BACK()));

        for (Ship ship : myFleet) {
            System.out.println("");
            System.out.println(String.format("Please enter the positions for the %s (size: %s)", ship.getName(), ship.getSize()));
            String previousPosition = "";
            for (int i = 1; i <= ship.getSize(); i++) {
                System.out.println(String.format("Enter position %s of %s (i.e A3):", i, ship.getSize()));

                String positionInput = scanner.next();
                if(!previousPosition.isEmpty() && !validateInput(ship, previousPosition, positionInput)) {
                    i = i - 1;
                    System.out.println(colorize("Invalid input, please make sure that all positions is in a horizontal or vertical row and gaps are not allowed", RED_TEXT()));
                    continue;
                }
                ship.addPosition(positionInput);
                previousPosition = positionInput;
                telemetry.trackEvent("Player_PlaceShipPosition", "Position", positionInput, "Ship", ship.getName(), "PositionInShip", Integer.valueOf(i).toString());
            }
        }
    }

    private static boolean validateInput(Ship currentShip, String previousPosition, String positionInput) {
        int prevLetterInt = previousPosition.toUpperCase().substring(0, 1).toCharArray()[0];
        int prevNumber = Integer.parseInt(previousPosition.substring(1));
        int curLetterInt = positionInput.toUpperCase().substring(0, 1).toCharArray()[0];
        int curNumber = Integer.parseInt(positionInput.substring(1));
        if(curLetterInt != prevLetterInt && curNumber != prevNumber) {
            return false;
        }
        if(curLetterInt - prevLetterInt > 1 && curNumber == prevNumber) {
            return false;
        }
        if(curNumber - prevNumber > 1 && curLetterInt == prevLetterInt) {
            return false;
        }
        return validateOverlap(myFleet, currentShip, positionInput);
    }

    private static boolean validateOverlap(List<Ship> fleet, Ship currentShip, String positionInput) {
        Letter letter = Letter.valueOf(positionInput.toUpperCase().substring(0, 1));
        int number = Integer.parseInt(positionInput.substring(1));
        Position inputPosition = new Position(letter, number);
        for (Ship ship : fleet) {
            if (ship == currentShip) {
                continue;
            }
            for (Position position : ship.getPositions()) {
                if (position.getColumn().toString() == inputPosition.getColumn().toString() && position.getRow() == inputPosition.getRow()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValidPosition(Ship ship) {
        for (Position position : ship.getPositions()){
            if(!validateOverlap(enemyFleet, ship, position.getColumn().toString() + position.getRow())) {
                return false;
            }
        }
        return true;
    }

    private static void InitializeEnemyFleet() {
        enemyFleet = GameController.initializeShips();
        Letter[] letters = Letter.values();
        do {
            int rnd = new Random().nextInt(letters.length);
            Letter letter = letters[rnd];
            int row = (int) Math.floor(Math.random() * 5) + 1;
            enemyFleet.get(0).setPositions(new ArrayList());
            enemyFleet.get(0).getPositions().add(new Position(letter, row));
            enemyFleet.get(0).getPositions().add(new Position(letter, row + 1));
            enemyFleet.get(0).getPositions().add(new Position(letter, row + 2));
            enemyFleet.get(0).getPositions().add(new Position(letter, row + 3));
            enemyFleet.get(0).getPositions().add(new Position(letter, row + 4));
            enemyFleet.get(0).getPositions().add(new Position(letter, row + 5));
        }
        while (!isValidPosition(enemyFleet.get(0)));

        do {
            int rnd = new Random().nextInt(letters.length);
            Letter letter = letters[rnd];
            int row = (int) Math.floor(Math.random() * 7) + 1;
            enemyFleet.get(1).setPositions(new ArrayList());
            enemyFleet.get(1).getPositions().add(new Position(letter, row));
            enemyFleet.get(1).getPositions().add(new Position(letter, row + 1));
            enemyFleet.get(1).getPositions().add(new Position(letter, row + 2));
            enemyFleet.get(1).getPositions().add(new Position(letter, row + 3));
        }
        while (!isValidPosition(enemyFleet.get(1)));

        do {
            int rnd = new Random().nextInt(letters.length - 3);
            Letter letter = letters[rnd];
            int row = (int) Math.floor(Math.random() * 10) + 1;
            enemyFleet.get(2).setPositions(new ArrayList());
            enemyFleet.get(2).getPositions().add(new Position(letter, row));
            enemyFleet.get(2).getPositions().add(new Position(letters[rnd + 1], row));
            enemyFleet.get(2).getPositions().add(new Position(letters[rnd + 2], row));
        }
        while (!isValidPosition(enemyFleet.get(2)));

        do {
            int rnd = new Random().nextInt(letters.length - 3);
            Letter letter = letters[rnd];
            int row = (int) Math.floor(Math.random() * 10) + 1;
            enemyFleet.get(3).setPositions(new ArrayList());
            enemyFleet.get(3).getPositions().add(new Position(letter, row));
            enemyFleet.get(3).getPositions().add(new Position(letters[rnd + 1], row));
            enemyFleet.get(3).getPositions().add(new Position(letters[rnd + 2], row));
        }
        while (!isValidPosition(enemyFleet.get(3)));

        do {
            int rnd = new Random().nextInt(letters.length - 3);
            Letter letter = letters[rnd];
            int row = (int) Math.floor(Math.random() * 10) + 1;
            enemyFleet.get(4).setPositions(new ArrayList());
            enemyFleet.get(4).getPositions().add(new Position(letter, row));
            enemyFleet.get(4).getPositions().add(new Position(letter, row + 1));
        }
        while (!isValidPosition(enemyFleet.get(4)));
    }
}
