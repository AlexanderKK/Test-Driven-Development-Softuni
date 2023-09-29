import org.junit.Before;
import org.junit.Test;
import status.TransactionStatus;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static status.TransactionStatus.*;

public class ChainblockImplTest {

    public static final Comparator<Transaction> AMOUNT_COMPARATOR = (f, s) -> Double.compare(s.getAmount(), f.getAmount());
    private Chainblock database;
    private Transaction transaction;

    @Before
    public void setUp() {
        database = new ChainblockImpl();
        transaction = new TransactionImpl(101, SUCCESSFUL, "Alex", "Michelle", 7.00);
    }

    @Test
    public void testAddTransactionShouldBeContained() {
        assertEquals(0, database.getCount());
        database.add(transaction);

        int expectedCount = 1;

        assertEquals(expectedCount, database.getCount());

        assertTrue(database.contains(transaction));

        assertTrue(database.contains(transaction.getId()));
    }

    @Test
    public void testAddTransactionShouldNotBeContained() {
        assertFalse(database.contains(transaction));

        assertFalse(database.contains(transaction.getId()));
    }

    @Test
    public void testChangeTransactionStatusById() {
        database.add(transaction);

        TransactionStatus expected = FAILED;

        database.changeTransactionStatus(transaction.getId(), FAILED);

        TransactionStatus actual = transaction.getStatus();

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeTransactionStatusByIdThrowsExceptionIfTransactionIsNotPresent() {
        database.changeTransactionStatus(transaction.getId(), FAILED);
    }

    @Test
    public void testRemoveTransactionById() {
        addTransactionsToDatabase();
        database.add(transaction);

        int expectedSize = database.getCount() - 1;

        database.removeTransactionById(transaction.getId());

        int actualSize = database.getCount();

        assertEquals(expectedSize, actualSize);
        assertFalse(database.contains(transaction));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTransactionByIdThrowsExceptionIfTransactionIsNotPresent() {
        database.removeTransactionById(transaction.getId());
    }

    @Test
    public void testGetByTransactionStatusShouldReturnCorrectAmountOfTransactions() {
        TransactionStatus expectedStatus = SUCCESSFUL;

        List<Transaction> expectedTransactions = getFilteredTransactionsByStatus(expectedStatus);

        List<Transaction> actualTransactions = iterableToListTransactions(database.getByTransactionStatus(expectedStatus));

        assertEquals(expectedTransactions.size(), actualTransactions.size());
    }

    @Test
    public void testGetByTransactionStatusShouldReturnAmountOfTransactionsOrderedByAmountDescending() {
        TransactionStatus expectedStatus = SUCCESSFUL;

        List<Transaction> expectedTransactions = getFilteredTransactionsByStatus(expectedStatus).stream()
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());

        List<Transaction> actualTransactions = iterableToListTransactions(database.getByTransactionStatus(expectedStatus));

        for (int i = 0; i < expectedTransactions.size(); i++) {
            Transaction expectedTransaction = expectedTransactions.get(i);
            Transaction actualTransaction = actualTransactions.get(i);

            System.out.println("Expected Transaction: " + expectedTransaction + " " + expectedTransaction.getAmount());
            System.out.println("Actual Transaction: " + actualTransaction + " " + actualTransaction.getAmount());
        }

        assertEquals(expectedTransactions, actualTransactions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByTransactionStatusShouldThrowIfNoExistingTransactionsWithGivenStatus() {
        iterableToListTransactions(database.getByTransactionStatus(NONE));
    }

    @Test
    public void testGetAllSendersWithTransactionStatusShouldReturnCorrectNumberOfSenders() {
        TransactionStatus expectedStatus = FAILED;

        List<String> expectedSenders = getFilteredTransactionsByStatus(expectedStatus).stream()
                .map(Transaction::getFrom)
                .collect(Collectors.toList());

        List<String> actualSenders = iterableToListStrings(database.getAllSendersWithTransactionStatus(expectedStatus));

        assertEquals(expectedSenders.size(), actualSenders.size());
    }

    @Test
    public void testGetAllSendersWithTransactionStatusShouldReturnSendersOrderedByTheirAmountOfTransactions() {
        TransactionStatus expectedStatus = FAILED;

        List<String> expectedSenders = getFilteredTransactionsByStatus(expectedStatus).stream()
                .sorted(AMOUNT_COMPARATOR)
                .map(Transaction::getFrom)
                .collect(Collectors.toList());

        List<String> actualSenders = iterableToListStrings(database.getAllSendersWithTransactionStatus(expectedStatus));

        assertEquals(expectedSenders.size(), actualSenders.size());
        assertEquals(expectedSenders, actualSenders);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllSendersWithTransactionStatusShouldThrowExceptionIfThereAreNoTransactions() {
        database.getAllSendersWithTransactionStatus(NONE);
    }

    @Test
    public void testGetAllReceiversWithTransactionStatusShouldReturnCorrectNumberOfReceivers() {
        TransactionStatus expectedStatus = FAILED;

        List<String> expectedReceivers = getFilteredTransactionsByStatus(expectedStatus).stream()
                .map(Transaction::getTo)
                .collect(Collectors.toList());

        List<String> actualReceivers = iterableToListStrings(database.getAllReceiversWithTransactionStatus(expectedStatus));

        assertEquals(expectedReceivers.size(), actualReceivers.size());
    }

    @Test
    public void testGetAllReceiversWithTransactionStatusShouldReturnReceiversOrderedByTheirAmountOfTransactions() {
        TransactionStatus expectedStatus = SUCCESSFUL;

        List<String> expectedReceivers = getFilteredTransactionsByStatus(expectedStatus).stream()
                .sorted(AMOUNT_COMPARATOR)
                .map(Transaction::getTo)
                .collect(Collectors.toList());

        List<String> actualReceivers = iterableToListStrings(database.getAllReceiversWithTransactionStatus(expectedStatus));

        assertEquals(expectedReceivers.size(), actualReceivers.size());
        assertEquals(expectedReceivers, actualReceivers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllReceiversWithTransactionStatusShouldThrowExceptionIfThereAreNoTransactions() {
        database.getAllReceiversWithTransactionStatus(NONE);
    }

    @Test
    public void testGetAllOrderedByAmountDescendingThenByIdShouldReturnTransactions() {
        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getAllOrderedByAmountDescendingThenById());

        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void testGetAllOrderedByAmountDescendingThenByIdShouldReturnTransactionsById() {
        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .sorted(AMOUNT_COMPARATOR.thenComparingInt(Transaction::getId).reversed())
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getAllOrderedByAmountDescendingThenById());

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetBySenderOrderedByAmountDescendingShouldReturnCorrectNumberOfTransactions() {
        addTransactionsToDatabase();
        String expectedSender = database.getById(1).getFrom();

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getFrom().equals(expectedSender))
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getBySenderOrderedByAmountDescending(expectedSender));

        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void testGetBySenderOrderedByAmountDescendingShouldReturnCorrectSequenceOfTransactions() {
        addTransactionsToDatabase();
        String expectedSender = database.getById(3).getFrom();

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getFrom().equals(expectedSender))
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getBySenderOrderedByAmountDescending(expectedSender));

        for (int i = 0; i < expected.size(); i++) {
            Transaction expectedTransaction = expected.get(i);
            Transaction actualTransaction = actual.get(i);

            System.out.println("Expected: " + expectedTransaction);
            System.out.println("Actual: " + actualTransaction);
        }

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBySenderOrderedByAmountDescendingShouldThrowExceptionWhenNoExistingTransactions() {
        String nonExistentSender = transaction.getFrom();
        database.getBySenderOrderedByAmountDescending(nonExistentSender);
    }

    @Test
    public void testGetByReceiverOrderedByAmountThenByIdShouldReturnCorrectNumberOfTransactions() {
        String expectedReceiver = "Michelle";

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getTo().equals(expectedReceiver))
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getByReceiverOrderedByAmountThenById(expectedReceiver));

        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void testGetByReceiverOrderedByAmountThenByIdShouldReturnTransactionsByAmountDescending() {
        String expectedReceiver = "Alex";

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getTo().equals(expectedReceiver))
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getByReceiverOrderedByAmountThenById(expectedReceiver));

        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void testGetByReceiverOrderedByAmountThenByIdShouldReturnTransactionsAmountDescIdAscending() {
        String expectedReceiver = "Alex";

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getTo().equals(expectedReceiver))
                .sorted(AMOUNT_COMPARATOR.thenComparingInt(Transaction::getId))
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getByReceiverOrderedByAmountThenById(expectedReceiver));

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByReceiverOrderedByAmountThenByIdShouldThrowIfNoExistingTransactions() {
        String expectedReceiver = "Alex";
        database.getByReceiverOrderedByAmountThenById(expectedReceiver);
    }

    @Test
    public void testGetByTransactionStatusAndMaximumAmountShouldReturnTransactionsLimitedToMaxAmount() {
        TransactionStatus expectedStatus = SUCCESSFUL;
        double maxAllowedAmount = 100;

        List<Transaction> expected = getFilteredTransactionsByStatus(expectedStatus).stream()
                .filter(t -> t.getAmount() <= maxAllowedAmount)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getByTransactionStatusAndMaximumAmount(expectedStatus, maxAllowedAmount));

        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void testGetByTransactionStatusAndMaximumAmountShouldReturnTransactionsByMaxAmountByAmountDesc() {
        TransactionStatus expectedStatus = FAILED;
        double maxAllowedAmount = 125.50;

        List<Transaction> expected = getFilteredTransactionsByStatus(expectedStatus).stream()
                .filter(t -> t.getAmount() <= maxAllowedAmount)
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getByTransactionStatusAndMaximumAmount(expectedStatus, maxAllowedAmount));

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetBySenderAndMinimumAmountDescendingShouldReturnCorrectNumberOfTransactions() {
        String expectedSender = "Pike";
        double minAllowedAmount = 45.30;

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getFrom().equals(expectedSender))
                .filter(t -> t.getAmount() > minAllowedAmount)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getBySenderAndMinimumAmountDescending(expectedSender, minAllowedAmount));

        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void testGetBySenderAndMinimumAmountDescendingShouldReturnTransactionsByAmountDesc() {
        String expectedSender = "Pike";
        double minAllowedAmount = 45.30;

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getFrom().equals(expectedSender))
                .filter(t -> t.getAmount() > minAllowedAmount)
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getBySenderAndMinimumAmountDescending(expectedSender, minAllowedAmount));

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBySenderAndMinimumAmountDescendingThrowsExceptionIfNoExistingTransactions() {
        database.getBySenderAndMinimumAmountDescending("None", 99.99);
    }

    @Test
    public void testGetByReceiverAndAmountRangeShouldReturnNumberOfTransactions() {
        String expectedReceiver = "Alex";
        double minAllowedAmount = 60.00;
        double maxAllowedAmount = 130.00;

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getTo().equals(expectedReceiver))
                .filter(t -> t.getAmount() >= minAllowedAmount && t.getAmount() < maxAllowedAmount)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(
                database.getByReceiverAndAmountRange(expectedReceiver, minAllowedAmount, maxAllowedAmount)
        );

        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void testGetByReceiverAndAmountRangeShouldReturnTransactionsByAmountDescThenByIdAsc() {
        String expectedReceiver = "Alex";
        double minAllowedAmount = 60.00;
        double maxAllowedAmount = 130.00;

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getTo().equals(expectedReceiver))
                .filter(t -> t.getAmount() >= minAllowedAmount && t.getAmount() < maxAllowedAmount)
                .sorted(AMOUNT_COMPARATOR.thenComparingInt(Transaction::getId))
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(
                database.getByReceiverAndAmountRange(expectedReceiver, minAllowedAmount, maxAllowedAmount)
        );

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByReceiverAndAmountRangeThrowsExceptionWhenNoExistingTransactions() {
        database.getByReceiverAndAmountRange("None", 13, 69);
    }

    @Test
    public void testGetAllInAmountRangeShouldReturnCorrectTransactionsBetweenMinAndMaxAmount() {
        double minAmount = 50.00;
        double maxAmount = 150.00;

        List<Transaction> expected = addTransactionsToDatabase().values().stream()
                .filter(t -> t.getAmount() >= minAmount && t.getAmount() <= maxAmount)
                .collect(Collectors.toList());

        List<Transaction> actual = iterableToListTransactions(database.getAllInAmountRange(minAmount, maxAmount));

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void testIteratorShouldReturnAllTransactions() {
        List<Transaction> expected = new ArrayList<>(addTransactionsToDatabase().values());

        Iterator<Transaction> iterator = database.iterator();
        assertNotNull(iterator);

        List<Transaction> actual = new ArrayList<>();
        iterator.forEachRemaining(actual::add);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    private List<Transaction> getFilteredTransactionsByStatus(TransactionStatus status) {
        return addTransactionsToDatabase().values()
                .stream()
                .filter(t -> t.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    private List<String> iterableToListStrings(Iterable<String> iterable) {
        List<String> list = new ArrayList<>();

        assertNotNull(iterable);
        iterable.forEach(list::add);

        return list;
    }

    private List<Transaction> iterableToListTransactions(Iterable<Transaction> iterable) {
        List<Transaction> list = new ArrayList<>();

        assertNotNull(iterable);
        iterable.forEach(list::add);

        return list;
    }

    private Map<Integer, Transaction> addTransactionsToDatabase() {
        Map<Integer, Transaction> transactions = new LinkedHashMap<>();

        transactions.put(1, new TransactionImpl(1, SUCCESSFUL, "Alex", "Michelle", 43.00));
        transactions.put(9, new TransactionImpl(9, ABORTED, "Mike", "Alex", 123.00));
        transactions.put(3, new TransactionImpl(3, FAILED, "Pike", "Alex", 155.00));
        transactions.put(4, new TransactionImpl(4, SUCCESSFUL, "Carol", "Mikey", 65.00));
        transactions.put(10, new TransactionImpl(10, UNAUTHORIZED, "Rick", "Mike", 23.00));
        transactions.put(6, new TransactionImpl(6, FAILED, "Porky", "Mark", 10.00));
        transactions.put(7, new TransactionImpl(7, SUCCESSFUL, "Michelle", "Alex", 155.00));
        transactions.put(8, new TransactionImpl(8, UNAUTHORIZED, "Pike", "Mike", 100.00));
        transactions.put(2, new TransactionImpl(2, FAILED, "Pike", "Alex", 123.00));
        transactions.put(5, new TransactionImpl(5, UNAUTHORIZED, "Rick", "Negan", 23.00));

        transactions.values().forEach(database::add);

        return transactions;
    }

}
