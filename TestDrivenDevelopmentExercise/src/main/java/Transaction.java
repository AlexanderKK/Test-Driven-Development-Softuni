import status.TransactionStatus;

public interface Transaction {

    void setId(int id);

    void setStatus(TransactionStatus status);

    void setFrom(String from);

    void setTo(String to);

    void setAmount(double amount);

    int getId();

    TransactionStatus getStatus();

    String getFrom();

    String getTo();

    double getAmount();

}
