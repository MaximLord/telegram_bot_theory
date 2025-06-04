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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    // мапа (хранение) для отслеживания состояния пользователя
    private final Map<Long, String> userStates = new HashMap<>();
    private final Map<Long, Task> draftTheories = new HashMap<>();

    // Клавиатура
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

    @Autowired
    private TaskRepository taskRepository;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        String messageText = update.getMessage().getText();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (update.hasMessage() && update.getMessage().hasText()) {
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

            if (userStates.containsKey(chatId)) {
                String state = userStates.get(chatId);
                Task draft = draftTheories.getOrDefault(chatId, new Task());

                switch (state) {
                    case "AWAITING_TOPIC":
                        draft.setTopic(messageText);
                        userStates.put(chatId, "AWAITING_QUESTION");
                        draftTheories.put(chatId, draft);
                        sendMessage(chatId, "Теперь введите вопрос: ");
                        break;

                    case "AWAITING_QUESTION":
                        draft.setQuestion(messageText);
                        userStates.put(chatId, "AWAITING_ANSWER");
                        draftTheories.put(chatId, draft);
                        sendMessage(chatId, "Теперь введите ответ: ");
                        break;

                    case "AWAITING_ANSWER":
                        draft.setAnswer(messageText);
                        taskRepository.save(draft);
                        userStates.remove(chatId);
                        draftTheories.remove(chatId);
                        sendMessage(chatId, "Теория успешно добавлена! ");
                        break;
                }
                return;
            }
        }

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.startsWith("topic_")) {
                String topic = callbackData.replace("topic_", "");
                sendTopicContent(chatId, topic);
            } else if (callbackData.equals("add_theory")) {
                startAddTheoryFlow(chatId);
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

    // Кнопка "Теория", выводит список тем
    private void sendMessageTheory(long chatId) {
        List<String> topics = taskRepository.findUniqueTopics();

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(topics.isEmpty() ? "Теорий пока нет " : "Выберите тему: ");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Вывод существующих тем
        for (String topic : topics) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(topic);
            button.setCallbackData("topic_" + topic);
            rows.add(Collections.singletonList(button));
        }

        // Кнопка добавление новой теории
        InlineKeyboardButton addButton = new InlineKeyboardButton();
        addButton.setText("Добавить теорию ");
        addButton.setCallbackData("add_theory");
        rows.add(Collections.singletonList(addButton));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {

    }

    // Отправить содержание темы
    private void sendTopicContent(Long chatId, String topic) {
        List<Task> tasks = taskRepository.findByTopic(topic);

        StringBuilder response = new StringBuilder();
        response.append("Тема: ").append(topic).append("\n\n");

        for (Task task : tasks) {
            response.append("Вопрос: ").append(task.getQuestion())
                    .append("\n Ответ: ").append(task.getAnswer())
                    .append("\n\n");
        }
        sendMessage(chatId, response.toString());
    }

    // Процесс добавления
    private void startAddTheoryFlow(Long chatId) {
        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        userStates.put(chatId, "AWAITING_TOPIC");
        sendMessage(chatId, "Введите название темы:");

        // Кнопка отмены

        cancelButton.setText("Отмена ");
        cancelButton.setCallbackData("cancel_add");
        markup.setKeyboard(List.of(List.of(cancelButton)));

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Введите название темы");
        message.setReplyMarkup(markup);
    }

    // Кнопка "Практика"
    private void sendMessagePractice(long chatId) {
    }
}






