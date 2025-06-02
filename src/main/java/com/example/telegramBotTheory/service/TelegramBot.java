package com.example.telegramBotTheory.service;

import com.example.telegramBotTheory.entity.BotConfig;
import com.example.telegramBotTheory.entity.Task;
import com.example.telegramBotTheory.repository.TaskRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(); // Клавиатура

    @Autowired
    private TaskRepository taskRepository;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendMessage(chatId, "Привет, " + update.getMessage().getChat().getFirstName() + "!");
                SendMessage messageWithKeyboard = keyboard(chatId, "Выберите режим: ");
                try {
                    execute(messageWithKeyboard); // Отправка клавиатуры
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;

            } else {
                sendMessage(chatId, "Бро, я не знаю такую команду ");
            }
        }

        // Обработка кнопок
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            switch (callbackData) {
                case "Теория":
                    //sendMessageTheory(chatId);
                    break;
                case "Практика":
                    sendMessagePractice(chatId);
                    break;
                default:
                    sendMessage(chatId, "Бро, я не знаю такую команду ");
            }
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

//Метод для команды /start
//    private void startCommandReceived(Long chatId, String name) {
//        String answer = "Привет " + name + "!";
//        log.info("Replied to user " + name);
//
//        sendMessage(chatId, answer);
//
//    }

    // Метод для отправки сообщений
    private void sendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Кнопки под текстом
    private SendMessage keyboard(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        // 1 Кнопка
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Теория");
        button1.setCallbackData("callback_data_for_button_1"); // Данные, которые придут при нажатии
        // 2 Кнопка
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Практика");
        button2.setCallbackData("callback_data_for_button_2");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row_1 = new ArrayList<>();
        row_1.add(button1);
        row_1.add(button2);
        rows.add(row_1);

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(rows);
        message.setReplyMarkup(inlineKeyboard);

        return message;
    }

    // Кнопка "Теория"
    private void sendMessageTheory(long chatId, Task task) {

    }

    private void addTheory(long chatId, Task task) {
        Optional<Task> optionalTask = taskRepository.findByQuestion(task.getQuestion());
        if (optionalTask.isPresent()) {
            throw new IllegalStateException("Задача с таким вопросом уже существует!");
        }
        //return taskRepository.save(chatId, task);
    }

    // Кнопка "Практика"
    private void sendMessagePractice(long chatId) {
    }
}






