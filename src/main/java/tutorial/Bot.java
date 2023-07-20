package tutorial;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
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

    public Bot() {
        this.quiz = new Quiz(); // Initialize the quiz instance
        this.gamestate = false; // Initialize the gamestate to false
        this.gamelevel = 0; // Initialize the gamelevel to 0
    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        var txt = msg.getText();

        if (update.hasMessage()) {
            if (txt.equals("/startgame")) {
                sendMenu(id, "Hello to the game \n\t Who wants to be a millionaire! ", createWelcomingMenu());
                gamestate = true;
            } else {
                //sendMessage(id, "I don't understand you");
            }
        }

        if (gamestate) {
            if (txt.equals("Lets start with the first question")) {
                Quiz quiz = new Quiz();
                currentQuestion = quiz.getRandomQuestion();
                sendMenu(id, currentQuestion.getText(), createPlayMenu(currentQuestion));
            }
        }

        if (currentQuestion.getSolution().equals(txt)) {
            gamelevel++;
            System.out.println("You are right");
        }
    }


    public void sendMenu(Long who, String txt, ReplyKeyboardMarkup buttons) {
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(buttons).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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

}
