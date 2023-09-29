import status.TransactionStatus;

import java.util.Objects;

public class TransactionImpl implements Comparable<TransactionImpl>, Transaction {

    private int id;
    private TransactionStatus status;
    private String from;
    private String to;
    private double amount;

    public TransactionImpl(int id, TransactionStatus status, String from, String to, double amount) {
        this.id = id;
        this.status = status;
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    public int compareTo(TransactionImpl other) {
        return Integer.compare(this.getId(), other.getId());
    }

    @Override
    public String toString() {
        return String.format("id: %d, status: %s from: %s, to: %s, amount: %.2f", id, status.name(), from, to, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TransactionImpl)) return false;

        TransactionImpl that = (TransactionImpl) o;

        return getId() == that.getId() && Double.compare(that.getAmount(), getAmount()) == 0 && getStatus() == that.getStatus() && getFrom().equals(that.getFrom()) && getTo().equals(that.getTo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStatus(), getFrom(), getTo(), getAmount());
    }

}
