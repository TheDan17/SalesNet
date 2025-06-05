package com.thedan17.salesnet;

import com.thedan17.salesnet.core.dao.AccGroupLinkRepository;
import com.thedan17.salesnet.core.dao.AccountRepository;
import com.thedan17.salesnet.core.object.dto.AccountInfoDto;
import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.object.dto.AccountUpdateDto;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.object.entity.AccGroupLink;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.core.service.AccountService;
import com.thedan17.salesnet.exception.ContentNotFoundException;
import com.thedan17.salesnet.exception.SuchElementExistException;
import com.thedan17.salesnet.util.EntityMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
  @Mock
  private AccountRepository accountRepository;
  @Mock
  private EntityMapper entityMapper;
  @Mock
  private AccGroupLinkRepository accGroupLinkRepository;
  @InjectMocks
  private AccountService accountService;

  @Test
  void addAccount_shouldSaveAndReturnAccountInfo() {
    // arrange
    var signupDto = new AccountSignupDto("loginuser1", "example@mail.by",
            "theBestPassword1423", "userFirstName", "userSecondName", "Physical");
    var datetimenow = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    var accountEntity = new Account(null, "loginuser1", "example@mail.by",
            null, "userFirstName", "userSecondName", "Physical", datetimenow); // маппер возвращает это
    var savedAccount = new Account(5L, "loginuser1", "example@mail.by",
            "fd9s0fj09j3209fjds;fj0932j0wjf", "userFirstName", "userSecondName", "Physical", datetimenow); // dao.save возвращает это
    var expectedInfo = new AccountInfoDto(5L, "userFirstName userSecondName",
            "userFirstName", "userSecondName", "loginuser1", "example@mail.by", "Physical", datetimenow);

    when(entityMapper.loginDtoToAccount(signupDto)).thenReturn(accountEntity);
    when(accountRepository.save(accountEntity)).thenReturn(savedAccount);
    when(entityMapper.accountToInfoDto(savedAccount)).thenReturn(expectedInfo);

    Optional<AccountInfoDto> result = accountService.addAccount(signupDto);

    assertTrue(result.isPresent());
    assertEquals(expectedInfo, result.get());
    verify(accountRepository).save(accountEntity);
  }

  @Test
  void addAccount_shouldReturnEmpty_whenDataIntegrityViolationOccurs() {
    var signupDto = new AccountSignupDto("loginuser1", "example@mail.by",
            "theBestPassword1423", "userFirstName", "userSecondName", "Physical");
    var accountEntity = new Account(5L, "loginuser1", "example@mail.by",
            "fd9s0fj09j3209fjds;fj0932j0wjf", "userFirstName", "userSecondName", "Physical", LocalDateTime.now()); // dao.save возвращает это

    when(entityMapper.loginDtoToAccount(signupDto)).thenReturn(accountEntity);
    when(accountRepository.save(accountEntity)).thenThrow(DataIntegrityViolationException.class);

    assertThrows(SuchElementExistException.class, ()->accountService.addAccount(signupDto));
  }

  @Test
  void getAccountByIdTest() {
    Long idTrue = 5L, idFalse = -10L;
    var accountDto = new AccountInfoDto(5L, "userFirstName userSecondName",
            "userFirstName", "userSecondName", "loginuser1", "example@mail.by", "Physical", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    var accountTrue = new Account(5L, "loginuser1", "example@mail.by",
            "fd9s0fj09j3209fjds;fj0932j0wjf", "userFirstName", "userSecondName", "Physical", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

    when(accountRepository.findById(idTrue)).thenReturn(Optional.of(accountTrue));
    when(entityMapper.accountToInfoDto(accountTrue)).thenReturn(accountDto);
    when(accountRepository.findById(idFalse)).thenReturn(Optional.empty());

    Optional<AccountInfoDto> trueEvent = accountService.getAccountById(idTrue),
                            falseEvent = accountService.getAccountById(idFalse);
    assertThat(falseEvent).isEmpty();
    assertThat(trueEvent).isPresent();
    assertEquals(trueEvent.get(), accountDto);
  }

  @Test
  void getAccountGroupsTest() {
    Long idFalse = -90L, idTrue = 32L;
    Account accountDummy = new Account();
    Group group1 = new Group("groupname1", ""), group2 = new Group("groupname2", "The Description"), group3 = new Group("groupname3", "");
    List<AccGroupLink> links = new ArrayList<>();
    links.add(new AccGroupLink(2L, group1, accountDummy, null));
    links.add(new AccGroupLink(3L, group2, accountDummy, null));
    links.add(new AccGroupLink(4L, group3, accountDummy, null));
    List<GroupIdDto> groups = new ArrayList<>();
    groups.add(new GroupIdDto(23L, "groupname1", "", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), 1L));
    groups.add(new GroupIdDto(23L, "groupname2", "The Description", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), 1L));
    groups.add(new GroupIdDto(23L, "groupname3", "", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), 1L));

    when(accountRepository.findById(idFalse)).thenReturn(Optional.empty());
    when(accountRepository.findById(idTrue)).thenReturn(Optional.of(accountDummy));
    when(accGroupLinkRepository.findByAccount(accountDummy)).thenReturn(links);
    when(entityMapper.groupToIdDto(group1)).thenReturn(groups.get(0));
    when(entityMapper.groupToIdDto(group2)).thenReturn(groups.get(1));
    when(entityMapper.groupToIdDto(group3)).thenReturn(groups.get(2));

    Optional<List<GroupIdDto>> result = accountService.getAccountGroups(idTrue),
                              result2 = accountService.getAccountGroups(idFalse);

    assertEquals(result.get(), groups);
    assertThat(result2).isEmpty();
  }

  @Test
  void searchAccounts_shouldReturnEmpty_whenAllParamsNull() {
    List<AccountInfoDto> result = accountService.searchAccounts(null, null, null);
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(accountRepository, never()).findAll((Example<Account>)any());
  }


  @Test
  void searchAccounts_shouldBuildSpecAndMapResults_whenParamsProvided() {
    String firstName = "Alice";
    String secondName = "Smith";
    String type = "USER";

    Account account1 = new Account();
    Account account2 = new Account();

    List<Account> foundAccounts = List.of(account1, account2);

    when(accountRepository.findAll(any(Specification.class))).thenReturn(foundAccounts);
    when(entityMapper.accountToInfoDto(account1)).thenReturn(new AccountInfoDto());
    when(entityMapper.accountToInfoDto(account2)).thenReturn(new AccountInfoDto());

    List<AccountInfoDto> result =
            accountService.searchAccounts(firstName, secondName, type);

    assertEquals(2, result.size());

    verify(accountRepository, times(1)).findAll(any(Specification.class));
    verify(entityMapper, times(2)).accountToInfoDto(any());
  }


  @Test
  void deleteAccount_shouldThrowException_whenAccountNotFound() {
    Long id = 1L;
    when(accountRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ContentNotFoundException.class, () -> accountService.deleteAccount(id));
    verify(accountRepository, never()).deleteById(any());
  }

  @Test
  void deleteAccount_shouldDeleteGroupsAndAccount_whenAccountExists() {
    Long id = 1L;
    Account account = new Account();
    account.setId(id);

    List<AccGroupLink> links = List.of(mock(AccGroupLink.class), mock(AccGroupLink.class));

    when(accountRepository.findById(id)).thenReturn(Optional.of(account));
    when(accGroupLinkRepository.findByAccount(account)).thenReturn(links);
    when(accountRepository.findById(id)).thenReturn(Optional.of(account)); // для getAccountEntityById()

    boolean result = accountService.deleteAccount(id);

    verify(accGroupLinkRepository, times(1)).findByAccount(account);
    verify(accGroupLinkRepository, times(links.size())).delete(any());
    verify(accountRepository, times(2)).findById(id); // дважды вызывается
    verify(accountRepository, times(1)).deleteById(id);

    assertTrue(result);
  }

  @Test
  void deleteAccount_shouldReturnFalse_whenSecondFindFails() {
    Long id = 1L;
    Account account = new Account();
    account.setId(id);

    List<AccGroupLink> links = List.of();

    when(accountRepository.findById(id))
            .thenReturn(Optional.of(account))    // первый вызов — для основного if
            .thenReturn(Optional.empty());       // второй вызов — для getAccountEntityById()

    when(accGroupLinkRepository.findByAccount(account)).thenReturn(links);

    boolean result = accountService.deleteAccount(id);

    verify(accountRepository, times(2)).findById(id);
    verify(accountRepository, never()).deleteById(any());
    assertFalse(result);
  }

  @Test
  void updateAccount_shouldUpdateAndReturnDto_whenAccountExists() {
    Long id = 1L;
    Account existingAccount = new Account();
    existingAccount.setId(id);
    existingAccount.setFirstName("Old");
    existingAccount.setSecondName("Name");
    existingAccount.setType("user");

    AccountUpdateDto updateDto = new AccountUpdateDto();
    updateDto.setFirstName("New");
    updateDto.setSecondName("Surname");
    updateDto.setType("admin");

    Account mappedAccount = new Account();
    mappedAccount.setFirstName("New");
    mappedAccount.setSecondName("Surname");

    Account updatedAccount = new Account(); // mocked as result of save()
    updatedAccount.setId(id);
    updatedAccount.setFirstName("New");
    updatedAccount.setSecondName("Surname");
    updatedAccount.setType("admin");

    AccountInfoDto infoDto = new AccountInfoDto();
    infoDto.setId(id);
    infoDto.setFirstName("New");

    when(accountRepository.findById(id)).thenReturn(Optional.of(existingAccount));
    when(entityMapper.updateDtoToAccount(updateDto)).thenReturn(mappedAccount);
    when(accountRepository.save(existingAccount)).thenReturn(updatedAccount);
    when(entityMapper.accountToInfoDto(updatedAccount)).thenReturn(infoDto);

    Optional<AccountInfoDto> result = accountService.updateAccount(id, updateDto);

    assertTrue(result.isPresent());
    assertEquals("New", result.get().getFirstName());
    verify(accountRepository).findById(id);
    verify(accountRepository).save(existingAccount);
    verify(entityMapper).updateDtoToAccount(updateDto);
    verify(entityMapper).accountToInfoDto(updatedAccount);
  }

  @Test
  void updateAccount_shouldReturnEmpty_whenAccountNotFound() {
    Long id = 1L;
    AccountUpdateDto updateDto = new AccountUpdateDto();

    when(accountRepository.findById(id)).thenReturn(Optional.empty());

    Optional<AccountInfoDto> result = accountService.updateAccount(id, updateDto);

    assertTrue(result.isEmpty());
    verify(accountRepository).findById(id);
    verifyNoMoreInteractions(accountRepository, entityMapper);
  }

  @Test
  void updateAccount_shouldReturnEmpty_whenFindThrowsException() {
    Long id = 1L;
    AccountUpdateDto updateDto = new AccountUpdateDto();

    when(accountRepository.findById(id)).thenThrow(new RuntimeException("DB error"));

    Optional<AccountInfoDto> result = accountService.updateAccount(id, updateDto);

    assertTrue(result.isEmpty());
    verify(accountRepository).findById(id);
    verifyNoMoreInteractions(accountRepository, entityMapper);
  }

  // lambda branches tests \/

  @Test
  void searchAccounts_shouldReturnEmptyList_whenAllFiltersNull() {
    List<AccountInfoDto> result = accountService.searchAccounts(null, null, null);
    assertTrue(result.isEmpty());
    verifyNoInteractions(accountRepository);
  }

  @Test
  void buildAccountSpecification_shouldReturnCorrectPredicate() {
    String firstName = "John";
    String secondName = "Doe";
    String type = "admin";

    Specification<Account> spec = accountService.buildAccountSpecification(firstName, secondName, type);

    // Моки для аргументов toPredicate
    Root<Account> root = mock(Root.class);
    CriteriaQuery<?> query = mock(CriteriaQuery.class);
    CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

    // Моки для путей (root.get("..."))
    Path<String> firstNamePath = mock(Path.class);
    Path<String> secondNamePath = mock(Path.class);
    Path<String> typePath = mock(Path.class);

    doReturn(firstNamePath).when(root).get("firstName");
    doReturn(secondNamePath).when(root).get("secondName");
    doReturn(typePath).when(root).get("type");

    // Моки для Predicate, которые возвращает criteriaBuilder.equal
    Predicate firstNamePredicate = mock(Predicate.class);
    Predicate secondNamePredicate = mock(Predicate.class);
    Predicate typePredicate = mock(Predicate.class);

    when(criteriaBuilder.equal(firstNamePath, firstName)).thenReturn(firstNamePredicate);
    when(criteriaBuilder.equal(secondNamePath, secondName)).thenReturn(secondNamePredicate);
    when(criteriaBuilder.equal(typePath, type)).thenReturn(typePredicate);

    // Мок для результата criteriaBuilder.and
    Predicate combinedPredicate = mock(Predicate.class);
    when(criteriaBuilder.and(firstNamePredicate, secondNamePredicate, typePredicate))
            .thenReturn(combinedPredicate);

    // Вызов тестируемого метода
    Predicate result = spec.toPredicate(root, query, criteriaBuilder);

    // Проверки
    assertSame(combinedPredicate, result);
    verify(root).get("firstName");
    verify(root).get("secondName");
    verify(root).get("type");
    verify(criteriaBuilder).equal(firstNamePath, firstName);
    verify(criteriaBuilder).equal(secondNamePath, secondName);
    verify(criteriaBuilder).equal(typePath, type);
    verify(criteriaBuilder).and(firstNamePredicate, secondNamePredicate, typePredicate);
  }

  @Test
  void buildAccountSpecification_shouldReturnEmptyPredicateWhenAllNull() {
    Specification<Account> spec = accountService.buildAccountSpecification(null, null, null);

    Root<Account> root = mock(Root.class);
    CriteriaQuery<?> query = mock(CriteriaQuery.class);
    CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

    // criteriaBuilder.and() без аргументов возвращает true-предикат, мокируем
    Predicate emptyPredicate = mock(Predicate.class);
    when(criteriaBuilder.and()).thenReturn(emptyPredicate);

    Predicate result = spec.toPredicate(root, query, criteriaBuilder);

    assertSame(emptyPredicate, result);
    verify(criteriaBuilder).and();  // вызов и проверка, что вызвали именно этот вариант
  }
}

