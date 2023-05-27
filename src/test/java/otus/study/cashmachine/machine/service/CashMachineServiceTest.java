package otus.study.cashmachine.machine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import otus.study.cashmachine.bank.dao.CardsDao;
import otus.study.cashmachine.bank.data.Card;
import otus.study.cashmachine.bank.service.AccountService;
import otus.study.cashmachine.bank.service.impl.CardServiceImpl;
import otus.study.cashmachine.machine.data.CashMachine;
import otus.study.cashmachine.machine.data.MoneyBox;
import otus.study.cashmachine.machine.service.impl.CashMachineServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static otus.study.cashmachine.TestUtil.getHash;

@ExtendWith(MockitoExtension.class)
class CashMachineServiceTest {

    @Spy
    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private CardsDao cardsDao;

    @Mock
    private AccountService accountService;

    @Mock
    private MoneyBoxService moneyBoxService;

    private CashMachineServiceImpl cashMachineService;

    private CashMachine cashMachine = new CashMachine(new MoneyBox());

    @BeforeEach
    void init() {
        cashMachineService = new CashMachineServiceImpl(cardService, accountService, moneyBoxService);
    }


    @Test
    void getMoney() {
        doReturn(BigDecimal.TEN).when(cardService).getMoney("1111", "2222", BigDecimal.TEN);
        when(moneyBoxService.getMoney(any(), anyInt())).thenReturn(List.of(1, 1, 1, 1));

        List<Integer> result = cashMachineService.getMoney(cashMachine, "1111", "2222", BigDecimal.TEN);
        assertEquals(List.of(1, 1, 1, 1), result);
    }

    @Test
    void putMoney() {
        BigDecimal expectedSum = BigDecimal.valueOf(2300);
        String pin = "0000";
        Card card = new Card(1L, "1111", 1L, getHash(pin));
        List<Integer> notes = List.of(0, 1, 2, 3);

        when(cardsDao.getCardByNumber(eq(card.getNumber()))).thenReturn(card);
        when(accountService.putMoney(eq(card.getId()), any())).thenReturn(expectedSum);

        BigDecimal result = cashMachineService.putMoney(cashMachine, card.getNumber(), pin, notes);

        verify(moneyBoxService).putMoney(any(),
                eq(notes.get(3)),
                eq(notes.get(2)),
                eq(notes.get(1)),
                eq(notes.get(0)));

        assertEquals(expectedSum, result);
    }

    @Test
    void checkBalance() {
        String pin = "0000";
        Card card = new Card(1L, "0000", 1L, getHash(pin));
        when(cardsDao.getCardByNumber(any())).thenReturn(card);
        when(accountService.checkBalance(any())).thenReturn(BigDecimal.TEN);

        BigDecimal result = cashMachineService.checkBalance(cashMachine, card.getNumber(), pin);
        assertEquals(BigDecimal.TEN, result);
    }

    @Test
    void changePin() {
        when(cardsDao.getCardByNumber("1111"))
                .thenReturn(new Card(1L, "1111", 1L, getHash("0000")));
        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        when(cardsDao.saveCard(captor.capture())).thenReturn(new Card(1L, "", 1L, ""));

        cashMachineService.changePin("1111", "0000", "0001");
        assertEquals(getHash("0001"), captor.getValue().getPinCode());
    }

    @Test
    void changePinWithAnswer() {
        when(cardsDao.getCardByNumber("1111"))
                .thenReturn(new Card(1L, "1111", 1L, getHash("0000")));

        List<Card> cards = new ArrayList<>();

        when(cardsDao.saveCard(any())).thenAnswer((Answer<Card>) invocation -> {
            cards.add(invocation.getArgument(0));
            return invocation.getArgument(0);
        });

        cashMachineService.changePin("1111", "0000", "0001");
        assertEquals(cards.get(0).getNumber(), "1111");
    }
}