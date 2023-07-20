package tutorial;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tutorial.quiz.Question;
import tutorial.quiz.Quiz;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private Quiz quiz;
    private boolean gamestate;
    private int gamelevel;
    private Question currentQuestion;
    private int[] pricepool;

    public Bot() {
        this.quiz = new Quiz(); // Initialize the quiz instance
        this.gamestate = false; // Initialize the gamestate to false
        this.gamelevel = 0; // Initialize the gamelevel to 0
        this.pricepool = new int[]{100, 1000, 16000, 64000, 1000000};
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        var txt = msg.getText();

        if (update.hasMessage()) {
            if (txt.equals("/startgame")){
                sendMenu(id, "Hello to the game\n\nWho wants to be a millionaire!\n\n " +
                        "We start with the 100€ question", createWelcomingMenu());
                gamelevel = 0;
                gamestate = true;
            } else if (txt.equals("Lets start again!")) {
                sendMenu(id, "Hello to the game \n\nWho wants to be a millionaire!\n\n " +
                        "We start with the 100€ question", createWelcomingMenu());
                gamelevel = 0;
                gamestate = true;
            }
                //sendMessage(id, "I don't understand you");
            }


        if (gamestate) {
            if (txt.equals("Lets start with the first question") || txt.equals("Lets start again!") || txt.equals("/startgame")) {
                currentQuestion = quiz.getRandomQuestion(gamelevel);
                sendMenu(id, currentQuestion.getText(), createPlayMenu(currentQuestion));
            } else if (txt.equals(currentQuestion.getSolution())) {
                gamelevel++;
                if (gamelevel >= quiz.getQuestionCategories().size()) {
                    sendMenu(id, "Congratulations! You answered all questions correctly!", startOver());
                    gamestate = false;
                    gamelevel = 0;
                } else {
                    sendMenu(id, "You are right! \n\tLets go to the next question. \n\t" +
                            "This question is worth " + pricepool[gamelevel] + "€", createPlayMenu(currentQuestion));
                    currentQuestion = quiz.getRandomQuestion(gamelevel);
                    sendMenu(id, currentQuestion.getText(), createPlayMenu(currentQuestion));
                }
            } else {
                // Wrong answer
                sendMenu(id, "Sorry, wrong answer! Game ends now.", startOver());
                gamestate = false;
                gamelevel = 0;
            }

        }
    }

    public void sendMenu(Long who, String txt, ReplyKeyboardMarkup buttons) {
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(buttons != null ? buttons : new ReplyKeyboardRemove()).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    public ReplyKeyboardMarkup startOver() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Lets start again!"));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup createPlayMenu(Question question) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String option : question.getOptions()) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(option));
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup createWelcomingMenu() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Lets start with the first question"));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

}

