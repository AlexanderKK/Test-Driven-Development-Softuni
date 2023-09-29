import status.TransactionStatus;

import java.util.*;
import java.util.stream.Collectors;

public class ChainblockImpl implements Chainblock {

    public static final Comparator<Transaction> AMOUNT_COMPARATOR = Comparator.comparingDouble(Transaction::getAmount).reversed();
    private Map<Integer, Transaction> database;

    public ChainblockImpl() {
        this.database = new LinkedHashMap<>();
    }

    private void ensureId(int id, String message) {
        if(!database.containsKey(id)) {
            throw new IllegalArgumentException(message);
        }
    }

    private List<Transaction> getFilteredTransactionsByStatus(TransactionStatus status) {
        return database.values()
                .stream()
                .filter(t -> t.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    private void ensureExistingTransactionsBySenderOrReceiver(String prefix, String senderOrReceiver) {
        if(database.values().stream().noneMatch(t -> t.getFrom().equals(senderOrReceiver))) {
            throw new IllegalArgumentException(prefix + senderOrReceiver + " cannot be found in the database");
        }
    }

    public int getCount() {
        return database.size();
    }

    public void add(Transaction transaction) {
        database.putIfAbsent(transaction.getId(), transaction);
    }

    public boolean contains(Transaction transaction) {
        return database.containsValue(transaction);
    }

    public boolean contains(int id) {
        return database.containsKey(id);
    }

    public void changeTransactionStatus(int id, TransactionStatus newStatus) {
        ensureId(id, "Cannot change status of non-existing transaction");

        Transaction transaction = getById(id);
        transaction.setStatus(newStatus);
    }

    public void removeTransactionById(int id) {
        ensureId(id, "Cannot remove transaction that does not exits");

        database.remove(id);
    }

    public Transaction getById(int id) {
        ensureId(id, "Cannot find transaction that does not exits");

        return database.get(id);
    }

    public Iterable<Transaction> getByTransactionStatus(TransactionStatus status) {
        List<Transaction> filteredTransactions = getFilteredTransactionsByStatus(status);

        if(filteredTransactions.size() == 0) {
            throw new IllegalArgumentException("Transactions with status " + status + " are not present");
        }

        filteredTransactions.sort(AMOUNT_COMPARATOR);

        return filteredTransactions;
    }

    public Iterable<String> getAllSendersWithTransactionStatus(TransactionStatus status) {
        List<Transaction> filteredTransactions = getFilteredTransactionsByStatus(status);

        if(filteredTransactions.size() == 0) {
            throw new IllegalArgumentException("There are no senders with status " + status);
        }

        return filteredTransactions.stream()
                .sorted(AMOUNT_COMPARATOR)
                .map(Transaction::getFrom)
                .collect(Collectors.toList());
    }

    public Iterable<String> getAllReceiversWithTransactionStatus(TransactionStatus status) {
        List<Transaction> filteredTransactions = getFilteredTransactionsByStatus(status);

        if(filteredTransactions.size() == 0) {
            throw new IllegalArgumentException("There are no receivers with status " + status);
        }

        return filteredTransactions.stream()
                .sorted(AMOUNT_COMPARATOR)
                .map(Transaction::getTo)
                .collect(Collectors.toList());
    }

    public Iterable<Transaction> getAllOrderedByAmountDescendingThenById() {
        return database.values().stream()
                .sorted(AMOUNT_COMPARATOR.thenComparingInt(Transaction::getId).reversed())
                .collect(Collectors.toList());
    }

    public Iterable<Transaction> getBySenderOrderedByAmountDescending(String sender) {
        ensureExistingTransactionsBySenderOrReceiver("Sender ", sender);

        return database.values().stream()
                .filter(t -> t.getFrom().equals(sender))
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());
    }

    public Iterable<Transaction> getByReceiverOrderedByAmountThenById(String receiver) {
        ensureExistingTransactionsBySenderOrReceiver("Receiver ", receiver);

        return database.values().stream()
                .filter(t -> t.getTo().equals(receiver))
                .sorted(AMOUNT_COMPARATOR.thenComparingInt(Transaction::getId))
                .collect(Collectors.toList());
    }

    public Iterable<Transaction> getByTransactionStatusAndMaximumAmount(TransactionStatus status, double amount) {
        return getFilteredTransactionsByStatus(status).stream()
                .filter(t -> t.getAmount() <= amount)
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());
    }

    public Iterable<Transaction> getBySenderAndMinimumAmountDescending(String sender, double amount) {
        ensureExistingTransactionsBySenderOrReceiver("Sender ", sender);

        return database.values().stream()
                .filter(t -> t.getFrom().equals(sender))
                .filter(t -> t.getAmount() > amount)
                .sorted(AMOUNT_COMPARATOR)
                .collect(Collectors.toList());
    }

    public Iterable<Transaction> getByReceiverAndAmountRange(String receiver, double lo, double hi) {
        ensureExistingTransactionsBySenderOrReceiver("Receiver ", receiver);

        return database.values().stream()
                .filter(t -> t.getTo().equals(receiver))
                .filter(t -> t.getAmount() >= lo && t.getAmount() < hi)
                .sorted(AMOUNT_COMPARATOR.thenComparingInt(Transaction::getId))
                .collect(Collectors.toList());
    }

    public Iterable<Transaction> getAllInAmountRange(double lo, double hi) {
        return database.values().stream()
                .filter(t -> t.getAmount() >= lo && t.getAmount() <= hi)
                .collect(Collectors.toList());
    }

    public Iterator<Transaction> iterator() {
        return database.values().iterator();
    }

}
