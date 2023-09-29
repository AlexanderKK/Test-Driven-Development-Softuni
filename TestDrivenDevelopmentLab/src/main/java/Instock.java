import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Instock implements ProductStock {

    private List<Product> products;

    public Instock() {
        this.products = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public boolean contains(Product product) {
        return products.contains(product);
    }

    @Override
    public void add(Product product) {
        products.add(product);
    }

    @Override
    public void changeQuantity(String label, int quantity) {
        Product product = findByLabel(label);
        product.setQuantity(quantity);
    }

    @Override
    public Product find(int index) {
        return products.get(index);
    }

    @Override
    public Product findByLabel(String label) {
        return products.stream()
                .filter(e -> e.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product with label " + label + " not found"));
    }

    @Override
    public Iterable<Product> findFirstByAlphabeticalOrder(int count) {
        if(count <= 0 || count > products.size()) {
            return new ArrayList<>();
        }

        return products.stream()
                .sorted()
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Product> findAllInRange(double begin, double end) {
        return products.stream()
                .filter(p -> p.getPrice() > begin && p.getPrice() <= end)
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .toList();
    }

    @Override
    public Iterable<Product> findAllByPrice(double price) {
        return products.stream()
                .filter(p -> p.getPrice() == price)
                .toList();
    }

    @Override
    public Iterable<Product> findFirstMostExpensiveProducts(int count) {
        if(count <= 0 || count > products.size()) {
            throw new IllegalArgumentException("Products are " + products.size() + " but requested are " + count);
        }

        return products
                .stream()
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .limit(count)
                .toList();
    }

    @Override
    public Iterable<Product> findAllByQuantity(int quantity) {
        return products.stream()
                .filter(p -> p.getQuantity() == quantity)
                .toList();
    }

    @Override
    public Iterator<Product> iterator() {
        return products.iterator();
    }

}
