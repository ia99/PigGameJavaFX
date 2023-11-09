import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PigGame extends Application {
    static int lastRoll = 0;
    static boolean gameRunning = false, player1Turn = false, isCpuOppo=false;
    static ArrayList<Integer> currentRoundList = new ArrayList<>();


    String player1Name, player2Name;
    int currentRound, player1Score, player2Score;

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }


    public PigGame() {
        this.player1Name="Guest1";
        this.player2Name="Guest2";
        this.currentRound=0;
        this.player1Score=0;
        this.player2Score=0;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Image[] diceResultsArray = new Image[7];
        //image array will have placeholder dice at 0 then numbered dice 1 through 6
        try {
            diceResultsArray[0] = new Image(new FileInputStream("src/dice/twodice.jpg"));
            for(int i=1; i<=6; i++) {
                diceResultsArray[i] = new Image(new FileInputStream("src/dice/"+i+"dice.png"));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        ImageView centerDice = new ImageView();
        centerDice.setImage(diceResultsArray[0]);

        primaryStage.setTitle("Pig");
        BorderPane border = new BorderPane();

        Button btnNewGame = new Button("New Game");
        Button btnHistory = new Button("View History");
        Text opponentTop = new Text("Opponent:");

        ToggleGroup toggleGroupTop = new ToggleGroup();
        RadioButton rbCpuTop = new RadioButton("CPU");
        rbCpuTop.setToggleGroup(toggleGroupTop);
        RadioButton rbHumanTop = new RadioButton("User");
        rbHumanTop.setToggleGroup(toggleGroupTop);
        rbHumanTop.setSelected(true);

        Pane paneTopSpacer = new Pane();
        HBox.setHgrow(paneTopSpacer, Priority.ALWAYS);

        HBox hboxTop = new HBox(btnNewGame, opponentTop, rbCpuTop, rbHumanTop, paneTopSpacer, btnHistory);
        hboxTop.setSpacing(10);
        hboxTop.setAlignment(Pos.CENTER);
        border.setTop(hboxTop);

        HBox hboxBottom = new HBox();

        TextField statusBottom = new TextField();
        statusBottom.setEditable(false);
        hboxBottom.getChildren().add(statusBottom);
        statusBottom.setAlignment(Pos.CENTER);
        hboxBottom.setAlignment(Pos.CENTER);
        border.setBottom(hboxBottom);

        VBox vboxLeft = new VBox();
        TextField totalScorePlayer1 = new TextField("0");
        totalScorePlayer1.setEditable(false);
        totalScorePlayer1.setAlignment(Pos.CENTER);
        border.setLeft(vboxLeft);
        totalScorePlayer1.setPrefWidth(45);

        Text textPlayer1Label = new Text("Player 1");
        textPlayer1Label.setTextAlignment(TextAlignment.LEFT);
        textPlayer1Label.setUnderline(true);

        TextField namePlayer1 = new TextField("Guest");
        namePlayer1.setAlignment(Pos.CENTER_LEFT);
        namePlayer1.setStyle("-fx-background-color: transparent");
        vboxLeft.getChildren().addAll(textPlayer1Label, namePlayer1, totalScorePlayer1);
        vboxLeft.setAlignment(Pos.TOP_LEFT);

        VBox vboxRight = new VBox();
        TextField totalScorePlayer2 = new TextField("0");
        totalScorePlayer2.setAlignment(Pos.CENTER);
        totalScorePlayer2.setEditable(false);
        totalScorePlayer2.setPrefWidth(45);

        Text textPlayer2Label = new Text("Player 2");
        textPlayer2Label.setTextAlignment(TextAlignment.RIGHT);
        textPlayer2Label.setUnderline(true);

        TextField namePlayer2 = new TextField("Guest2");
        namePlayer2.setAlignment(Pos.CENTER_RIGHT);
        namePlayer2.setEditable(true);
        namePlayer2.setStyle("-fx-background-color: transparent");
        vboxRight.getChildren().addAll(textPlayer2Label, namePlayer2, totalScorePlayer2);
        border.setRight(vboxRight);
        vboxRight.setAlignment(Pos.TOP_RIGHT);

        // listeners to ensure TextField change modifies playerName for correct logging
        namePlayer1.textProperty().addListener((obs, ov, nv) -> setPlayer1Name(nv));
        namePlayer2.textProperty().addListener((obs, ov, nv) -> setPlayer2Name(nv));

        StackPane stackPaneCenter = new StackPane();

        Button btnRollDice = new Button("Roll");
        Button btnHoldDice = new Button("Hold");

        Button btnDiceResult = new Button();
        btnDiceResult.setPrefWidth(45);

        Button currentRoundBottom = new Button();
        currentRoundBottom.setAlignment(Pos.CENTER);
        currentRoundBottom.setMaxWidth(45);

        stackPaneCenter.getChildren().addAll(btnRollDice, btnHoldDice, centerDice, currentRoundBottom);
        StackPane.setAlignment(btnRollDice, Pos.TOP_LEFT);
        StackPane.setAlignment(btnHoldDice, Pos.TOP_RIGHT);
        StackPane.setAlignment(centerDice,Pos.CENTER);
        StackPane.setAlignment(currentRoundBottom, Pos.BOTTOM_CENTER);

        border.setCenter(stackPaneCenter);

        rbHumanTop.setOnMouseClicked(e -> humanOppoClicked(namePlayer2));
        rbCpuTop.setOnMouseClicked(e -> cpuOppoClicked(namePlayer2));
        btnHistory.setOnMouseClicked(e -> viewHistoryClicked());
        btnRollDice.setOnMouseClicked(e -> rollDiceClicked(centerDice, diceResultsArray,totalScorePlayer1, totalScorePlayer2, currentRoundBottom, statusBottom));
        btnNewGame.setOnMouseClicked(e -> newGameClicked(centerDice,diceResultsArray, totalScorePlayer1, totalScorePlayer2, currentRoundBottom, rbHumanTop, rbCpuTop, statusBottom));
        btnHoldDice.setOnMouseClicked(e-> holdBtnClicked(centerDice,diceResultsArray, totalScorePlayer1,totalScorePlayer2,  currentRoundBottom, statusBottom));
        primaryStage.setScene(new Scene(border, 550, 400));
        primaryStage.show();
    }

    private void newGameClicked(ImageView centerDice, Image[] diceResultsArray, TextField totalScorePlayer1,TextField totalScorePlayer2, Button currentRoundBottom,  RadioButton humanRb, RadioButton cpuRb, TextField statusBottom) {
        centerDice.setImage(diceResultsArray[0]);
        totalScorePlayer1.setText("0");
        totalScorePlayer2.setText("0");
        currentRoundBottom.setText("0");
        lastRoll = 0;
        currentRoundList.clear();
        setPlayer1Score(0);
        setPlayer2Score(0);
        setCurrentRound(0);
        Random randomFirstTurn = new Random();
        player1Turn = randomFirstTurn.nextBoolean();
        gameRunning = true;
        if (humanRb.isSelected()) {
            isCpuOppo = false;
        } else if (cpuRb.isSelected()) {
            isCpuOppo = true;
        }
        if (player1Turn) {
            statusBottom.setText("Player 1 turn");
            totalScorePlayer1.setStyle("-fx-background-color: lightgreen");
            totalScorePlayer2.setStyle("-fx-background-color: white");
        }
        else {
            statusBottom.setText("Player 2 turn");
            totalScorePlayer1.setStyle("-fx-background-color: white");
            totalScorePlayer2.setStyle("-fx-background-color: lightgreen");
        }

        // if CPU goes first
        if (isCpuOppo && !player1Turn) {
            while (true) {
                lastRoll = (new Random().nextInt(6) + 1);
                centerDice.setImage(diceResultsArray[lastRoll]);
                if (lastRoll > 1) {
                    currentRoundList.add(lastRoll);
                    int sum=0;
                    for (Integer integer : currentRoundList) {
                        sum+=integer;
                    }
                    setCurrentRound(sum);
                    currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                    if (getCurrentRound() + getPlayer2Score() >= 100) {
                        statusBottom.setText("CPU WINS!!");
                        setPlayer2Score(getCurrentRound() + getPlayer2Score());
                        currentRoundList.clear();
                        gameRunning = false;
                        try {
                            BufferedWriter writer = new BufferedWriter(new FileWriter("PigLog.txt", true));
                            Date date = Calendar.getInstance().getTime();
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            String strDate = dateFormat.format(date);
                            writer.append(getPlayer2Name()).append(",").append(strDate).append(",").append(String.valueOf(getPlayer2Score())).append(",Win,");
                            writer.append(getPlayer1Name()).append(",").append(strDate).append(",").append(String.valueOf(getPlayer1Score())).append(",Lose,");
                            writer.close();
                        } catch (IOException e) {
                            // file is created at start
                        }
                    }
                    if(lastRoll==1) {
                        currentRoundList.clear();
                        setCurrentRound(0);
                        currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                        statusBottom.setText("CPU rolled 1");
                        break;
                    }
                    else if (getCurrentRound() >= 20) {
                        int scoreBefore = Integer.parseInt(totalScorePlayer2.getText());
                        totalScorePlayer2.setText(Integer.toString(scoreBefore + getCurrentRound()));
                        setPlayer2Score(scoreBefore + getCurrentRound());
                        player1Turn = true;
                        currentRoundList.clear();
                        setCurrentRound(0);
                        lastRoll = 0;
                        centerDice.setImage(diceResultsArray[0]);
                        currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                        statusBottom.setText("Player 1 turn");
                        totalScorePlayer1.setStyle("-fx-background-color: lightgreen");
                        totalScorePlayer2.setStyle("-fx-background-color: white");
                        break;
                    }
                }
            }
        }
    }


    private void viewHistoryClicked() {
        Stage historyStage = new Stage();
        TableView table = new TableView();
        TableColumn<PigGame, String> nameColumn = new TableColumn<>("Name");
        TableColumn<PigGame, String> dateColumn = new TableColumn<>("Date");
        TableColumn<PigGame, String> scoreColumn = new TableColumn<>("Score");
        TableColumn<PigGame, String> resultColumn = new TableColumn<>("Result");
        table.getColumns().addAll(nameColumn, dateColumn, scoreColumn, resultColumn);

        class TableData {

            public void setNameProperty(String nameProperty) {
                this.nameProperty.set(nameProperty);
            }

            public void setDateProperty(String dateProperty) {
                this.dateProperty.set(dateProperty);
            }

            public void setScoreProperty(String scoreProperty) {
                this.scoreProperty.set(scoreProperty);
            }

            public void setResultProperty(String resultProperty) {
                this.resultProperty.set(resultProperty);
            }

            final StringProperty nameProperty = new SimpleStringProperty();
            final StringProperty dateProperty = new SimpleStringProperty();
            final StringProperty scoreProperty = new SimpleStringProperty();
            final StringProperty resultProperty = new SimpleStringProperty();
        }

        try {
            Collection<TableData> list = Files.readAllLines(new File("PigLog.txt").toPath()).stream().map(line -> {
                String[] info = line.split(",");
                TableData td = new TableData();
                td.setNameProperty(info[0]);
                td.setDateProperty(info[1]);
                td.setScoreProperty(info[2]);
                td.setResultProperty(info[3]);
                return td;
            }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StackPane tablePane = new StackPane();
        historyStage.setTitle("Pig history");
        tablePane.getChildren().add(table);
        historyStage.setScene(new Scene(tablePane,400,400));
        historyStage.show();
    }

    private void humanOppoClicked(TextField namePlayer2) {
        namePlayer2.setEditable(true);
        if (namePlayer2.getText().equals("CPU")) {
            namePlayer2.setText("Guest2");
            setPlayer2Name("Guest2");
        }
    }

    private void cpuOppoClicked(TextField namePlayer2) {
        namePlayer2.setText("CPU");
        setPlayer2Name("CPU");
        namePlayer2.setEditable(false);
    }

    private void rollDiceClicked(ImageView centerDice, Image[] diceResultsArray, TextField totalScorePlayer1,TextField totalScorePlayer2, Button currentRoundBottom, TextField statusBottom) {
        if (!gameRunning) {
            statusBottom.setText("no game started");
            currentRoundList.clear();
        } else {
            lastRoll = (new Random().nextInt(6) + 1);
            centerDice.setImage(diceResultsArray[lastRoll]);
            if (lastRoll > 1) {
                currentRoundList.add(lastRoll);
                int sum = 0;
                for (Integer integer : currentRoundList) {
                    sum += integer;
                }
                setCurrentRound(sum);
                currentRoundBottom.setText(Integer.toString(sum));

                if (getCurrentRound() + getPlayer1Score() >= 100 && player1Turn) {
                    statusBottom.setText("Player 1 WINS!!");
                    setPlayer1Score(getCurrentRound() + getPlayer1Score());
                    currentRoundList.clear();
                    gameRunning = false;
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("PigLog.txt", true));
                        Date date = Calendar.getInstance().getTime();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String strDate = dateFormat.format(date);
                        writer.append(getPlayer1Name()).append(",").append(strDate).append(",").append(String.valueOf(getPlayer1Score())).append(",Win,");
                        writer.append(getPlayer2Name()).append(",").append(strDate).append(",").append(String.valueOf(getPlayer2Score())).append(",Lose,");
                        writer.close();
                    } catch (IOException e) {
                        // file is created at start
                    }
                }
                if (getCurrentRound() + getPlayer2Score() >= 100 &&  !player1Turn) {
                    statusBottom.setText("Player 2 WINS!!");
                    setPlayer2Score(getCurrentRound() + getPlayer2Score());
                    currentRoundList.clear();
                    gameRunning = false;
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("PigLog.txt", true));
                        Date date = Calendar.getInstance().getTime();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String strDate = dateFormat.format(date);
                        writer.append(getPlayer2Name()).append(",").append(strDate).append(",").append(String.valueOf(getPlayer2Score())).append(",Win,");
                        writer.append(getPlayer1Name()).append(",").append(strDate).append(",").append(String.valueOf(getPlayer1Score())).append(",Lose,");
                        writer.close();
                    } catch (IOException e) {
                        // file is created at start
                    }
                }
            }
            else if (lastRoll == 1) {
                currentRoundList.clear();
                centerDice.setImage(diceResultsArray[1]);
                setCurrentRound(0);
                currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                if (player1Turn) {
                    player1Turn = false;
                    statusBottom.setText("Player 2 turn");
                    totalScorePlayer1.setStyle("-fx-background-color: white");
                    totalScorePlayer2.setStyle("-fx-background-color: lightgreen");
                } else {
                    player1Turn = true;
                    statusBottom.setText("Player 1 turn");
                    totalScorePlayer1.setStyle("-fx-background-color: lightgreen");
                    totalScorePlayer2.setStyle("-fx-background-color: white");
                }
            }

            if (isCpuOppo && !player1Turn) {
                currentRoundList.clear();
                while (true) {
                    lastRoll = (new Random().nextInt(6) + 1);
                    centerDice.setImage(diceResultsArray[lastRoll]);
                    if (lastRoll > 1) {
                        currentRoundList.add(lastRoll);
                        int sum = 0;
                        for (Integer integer : currentRoundList) {
                            sum += integer;
                        }
                        setCurrentRound(sum);
                        currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                        if (getCurrentRound() + getPlayer2Score() >= 100) {
                            statusBottom.setText("CPU WINS!!");
                            setPlayer2Score(getCurrentRound() + getPlayer2Score());
                            currentRoundList.clear();
                            gameRunning = false;
                            try {
                                BufferedWriter writer = new BufferedWriter(new FileWriter("PigLog.txt", true));
                                Date date = Calendar.getInstance().getTime();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                String strDate = dateFormat.format(date);
                                writer.append(player2Name).append(",").append(strDate).append(",").append(String.valueOf(getPlayer2Score())).append(",Win,");
                                writer.append(player1Name).append(",").append(strDate).append(",").append(String.valueOf(getPlayer1Score())).append(",Lose,");
                                writer.close();
                            } catch (IOException e) {
                                // file is created at start
                            }
                            break;
                        }
                        // CPU hold if current round X or more
                        if (getCurrentRound() >= 15) {
                            int scoreBefore = Integer.parseInt(totalScorePlayer2.getText());
                            setPlayer2Score(scoreBefore + getCurrentRound());
                            totalScorePlayer2.setText(Integer.toString(scoreBefore + getCurrentRound()));
                            currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                            statusBottom.setText("CPU held at " + getCurrentRound());
                            player1Turn = true;
                            totalScorePlayer1.setStyle("-fx-background-color: lightgreen");
                            totalScorePlayer2.setStyle("-fx-background-color: white");
                            setCurrentRound(0);
                            currentRoundList.clear();
                            currentRoundBottom.setText("0");
                            lastRoll = 0;
                            centerDice.setImage(diceResultsArray[0]);
                            break;
                        }
                    } else if (isCpuOppo && !player1Turn && lastRoll == 1) {
                        currentRoundList.clear();
                        setCurrentRound(0);
                        currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                        statusBottom.setText("CPU rolled 1");
                        player1Turn=true;
                        totalScorePlayer1.setStyle("-fx-background-color: lightgreen");
                        totalScorePlayer2.setStyle("-fx-background-color: white");
                        break;
                    }
                }
            }
        }
    }

    private void holdBtnClicked(ImageView centerDice, Image[] diceResultsArray, TextField totalScorePlayer1, TextField totalScorePlayer2, Button currentRoundBottom, TextField statusBottom){
        if (!gameRunning) {
            statusBottom.setText("no game started");
        }
        else if(player1Turn) {
            int scoreBefore = Integer.parseInt(totalScorePlayer1.getText());
            totalScorePlayer1.setText(Integer.toString(scoreBefore + getCurrentRound()));
            setPlayer1Score(scoreBefore + getCurrentRound());
            player1Turn = false;
            currentRoundBottom.setText(Integer.toString(getCurrentRound()));
            currentRoundList.clear();
            setCurrentRound(0);
            currentRoundBottom.setText("0");
            lastRoll = 0;
            centerDice.setImage(diceResultsArray[0]);
            statusBottom.setText("Player 2 turn");
            totalScorePlayer1.setStyle("-fx-background-color: white");
            totalScorePlayer2.setStyle("-fx-background-color: lightgreen");
            if (isCpuOppo && !player1Turn) {
                while (true) {
                    lastRoll = (new Random().nextInt(6) + 1);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    centerDice.setImage(diceResultsArray[lastRoll]);
                    if (lastRoll > 1) {
                        currentRoundList.add(lastRoll);
                        int sum = 0;
                        for (Integer integer : currentRoundList) {
                            sum+=integer;
                        }
                        setCurrentRound(sum);
                        currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                        if (getCurrentRound() + getPlayer2Score() >= 100) {
                            statusBottom.setText("CPU WINS!!");
                            setPlayer2Score(getCurrentRound() + getPlayer2Score());
                            currentRoundList.clear();
                            gameRunning = false;
                            try {
                                BufferedWriter writer = new BufferedWriter(new FileWriter("PigLog.txt", true));
                                Date date = Calendar.getInstance().getTime();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                String strDate = dateFormat.format(date);
                                writer.append(player2Name).append(",").append(strDate).append(",").append(String.valueOf(getPlayer2Score())).append(",Win,");
                                writer.append(player1Name).append(",").append(strDate).append(",").append(String.valueOf(getPlayer1Score())).append(",Lose,");
                                writer.close();
                            } catch (IOException e) {
                                // file is created at start
                            } break;
                        }
                        // CPU hold if current round 20 or more
                        if(getCurrentRound()>=20) {
                            scoreBefore = Integer.parseInt(totalScorePlayer2.getText());
                            totalScorePlayer2.setText(Integer.toString(scoreBefore + getCurrentRound()));
                            setPlayer2Score(scoreBefore + getCurrentRound());
                            currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                            statusBottom.setText("CPU held at "+getCurrentRound());
                            player1Turn = true;
                            totalScorePlayer1.setStyle("-fx-background-color: lightgreen");
                            totalScorePlayer2.setStyle("-fx-background-color: white");
                            setCurrentRound(0);
                            currentRoundList.clear();
                            currentRoundBottom.setText("0");
                            lastRoll = 0;
                            centerDice.setImage(diceResultsArray[0]);
                            break;
                        }
                    } else if (isCpuOppo && !player1Turn && lastRoll==1) {
                        currentRoundList.clear();
                        setCurrentRound(0);
                        currentRoundBottom.setText(Integer.toString(getCurrentRound()));
                        statusBottom.setText("CPU rolled 1");
                    }
                }
            }
        }
        else if(!isCpuOppo){
            int scoreBefore = Integer.parseInt(totalScorePlayer2.getText());
            totalScorePlayer2.setText(Integer.toString(scoreBefore + getCurrentRound()));
            setPlayer2Score(scoreBefore + getCurrentRound());
            currentRoundBottom.setText(Integer.toString(getCurrentRound()));
            player1Turn = true;
            totalScorePlayer1.setStyle("-fx-background-color: lightgreen");
            totalScorePlayer2.setStyle("-fx-background-color: white");
            setCurrentRound(0);
            currentRoundList.clear();
            lastRoll = 0;
            centerDice.setImage(diceResultsArray[0]);
            currentRoundBottom.setText("0");
            statusBottom.setText("Player 1 turn");
        }
    }
}