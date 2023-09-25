package com.eg.tgcurrencybot.service;

import com.eg.tgcurrencybot.dto.ValuteCursOnDate;
import com.eg.tgcurrencybot.entity.ActiveChat;
import com.eg.tgcurrencybot.repository.ActiveChatRepository;
import com.eg.tgcurrencybot.repository.IncomeRepository;
import com.eg.tgcurrencybot.repository.SpendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.eg.tgcurrencybot.service.FinanceService.ADD_INCOME;
import static com.eg.tgcurrencybot.service.FinanceService.CURRENT_RATES;
import static com.eg.tgcurrencybot.service.FinanceService.ADD_SPEND;


@Service
@Slf4j
@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {
    private final SpendRepository spendRepository;
    private final IncomeRepository incomeRepository;
    private final ActiveChatRepository activeChatRepository;
    private final CentralRussianBankService centralBankRussianService;

    public String getPreviousCommands(Long chatId) {
        return previousCommands.get(chatId).get(previousCommands.get(chatId).size() - 1);
    }

    private void putPreviousCommand(Long chatId, String command) {
        if (previousCommands.get(chatId) == null) {
            List<String> commands = new ArrayList<>();
            commands.add(command);
            previousCommands.put(chatId, commands);
        } else {
            previousCommands.get(chatId).add(command);
        }
    }

    private Map<Long, List<String>> previousCommands = new ConcurrentHashMap<>();

    @Value("${bot.api.key}")
    private String apiKey;

    @Value("${bot.name}")
    private String name;

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        FinanceService financeService = new FinanceService(incomeRepository, spendRepository);
        try {
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            response.setChatId(String.valueOf(chatId));
            if (CURRENT_RATES.equalsIgnoreCase(message.getText())) {
                for (ValuteCursOnDate valuteCursOnDate : centralBankRussianService.getCurrenciesFromCbr()) {
                    response.setText(StringUtils.defaultIfBlank(response.getText(), "") + valuteCursOnDate.getName() + " - " + valuteCursOnDate.getCourse() + "\n");
                }
            } else if (ADD_INCOME.equalsIgnoreCase(message.getText())) {
                response.setText("Отправьте мне сумму полученного дохода");
            } else if (ADD_SPEND.equalsIgnoreCase(message.getText())) {
                response.setText("Отправьте мне сумму расходов");
            } else {
                response.setText(financeService.addFinanceOperation(getPreviousCommands(message.getChatId()), message.getText(), message.getChatId()));
            }

            putPreviousCommand(message.getChatId(), message.getText());
            execute(response);
            if (activeChatRepository.findActiveChatByChatId(chatId).isEmpty()) {
                ActiveChat activeChat = new ActiveChat();
                activeChat.setChatId(chatId);
                activeChatRepository.save(activeChat);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void start() {
        log.info("username: {}, token: {}", name, apiKey);
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return apiKey;
    }

    public void sendNotificationToAllActiveChats(String message, Set<Long> chatIds) {
        for (Long id : chatIds) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(id));
            sendMessage.setText(message);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
