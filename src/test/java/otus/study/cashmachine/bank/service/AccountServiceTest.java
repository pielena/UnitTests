package otus.study.cashmachine.bank.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import otus.study.cashmachine.bank.dao.AccountDao;
import otus.study.cashmachine.bank.data.Account;
import otus.study.cashmachine.bank.service.impl.AccountServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountService;

    @Test
    void createAccountMock() {
        Account expectedAccount = new Account(1L, BigDecimal.TEN);

        ArgumentMatcher<Account> matcher = argument ->
                argument.getAmount().compareTo(expectedAccount.getAmount()) == 0;

        when(accountDao.saveAccount(argThat(matcher))).thenReturn(expectedAccount);

        Account testAccount = accountService.createAccount(BigDecimal.TEN);
        assertEquals(expectedAccount.getAmount(), testAccount.getAmount());
    }

    @Test
    void createAccountCaptor() {
        Account expectedAccount = new Account(1L, BigDecimal.TEN);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        when(accountDao.saveAccount(captor.capture())).thenReturn(new Account(1, BigDecimal.TEN));

        accountService.createAccount(BigDecimal.TEN);
        assertEquals(expectedAccount.getAmount(), captor.getValue().getAmount());
    }

    @Test
    void addSum() {
        Account account = new Account(1L, BigDecimal.TEN);
        when(accountDao.getAccount(1L)).thenReturn(account);

        BigDecimal result = accountService.putMoney(account.getId(), BigDecimal.TEN);
        assertEquals(BigDecimal.valueOf(20), result);
    }

    @Test
    void getSum() {
        Account account = new Account(1L, BigDecimal.TEN);
        when(accountDao.getAccount(1L)).thenReturn(account);

        BigDecimal result = accountService.getMoney(account.getId(), BigDecimal.TEN);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getSumMoreThanHave() {
        Account account = new Account(1L, BigDecimal.ONE);
        when(accountDao.getAccount(1L)).thenReturn(account);

        Exception thrown = assertThrows(IllegalArgumentException.class,
                () -> accountService.getMoney(account.getId(), BigDecimal.TEN));
        assertEquals("Not enough money", thrown.getMessage());
    }

    @Test
    void getAccount() {
        Account expectedAccount = new Account(1L, BigDecimal.TEN);
        when(accountDao.getAccount(1L)).thenReturn(expectedAccount);

        Account result = accountService.getAccount(expectedAccount.getId());
        assertEquals(expectedAccount, result);
    }

    @Test
    void checkBalance() {
        Account account = new Account(1L, BigDecimal.TEN);
        when(accountDao.getAccount(account.getId())).thenReturn(account);

        BigDecimal result = accountService.checkBalance(account.getId());
        assertEquals(account.getAmount(), result);
    }
}
