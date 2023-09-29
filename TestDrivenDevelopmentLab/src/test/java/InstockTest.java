import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class InstockTest {

    private Instock instock;
    private Product product;

    @Before
    public void setUp() {
        instock = new Instock();
        product = new Product("label1", 10, 5);
    }

    @Test
    public void testAddProductShouldCheckIfItIsContained() {
        instock.add(product);
        assertTrue(instock.contains(product));
    }

    @Test
    public void testContainsShouldReturnFalseIfProductNotContained() {
        assertFalse(instock.contains(product));
    }

    @Test
    public void testCountShouldReturnProductsCount() {
        assertEquals(0, instock.getCount());
        instock.add(product);
        assertEquals(1, instock.getCount());
        instock.add(product);
        assertEquals(2, instock.getCount());
    }

    @Test
    public void testFindShouldReturnCorrectNthProduct() {
        List<Product> products = addMultipleProducts();

        Product expectedProduct = products.get(3);
        Product actualProduct = instock.find(3);

        assertNotNull(actualProduct);
        assertEquals(expectedProduct.getLabel(), actualProduct.getLabel());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFindShouldThrowIfIndexNotPresent() {
        List<Product> products = addMultipleProducts();
        instock.find(products.size());
    }

    @Test
    public void testChangeQuantityShouldUpdateQuantity() {
        instock.add(product);
        int expectedQuantity = product.getQuantity() + 13;
        instock.changeQuantity(product.getLabel(), expectedQuantity);

        assertEquals(expectedQuantity, product.getQuantity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeQuantityShouldFailIfProductWithLabelIsMissing() {
        instock.changeQuantity("no-such-label", 3);
    }

    @Test
    public void testFindByLabelShouldReturnProductWithTheSameLabel() {
        addMultipleProducts();
        instock.add(product);

        Product actual = instock.findByLabel(product.getLabel());
        assertNotNull(actual);
        assertEquals(product.getLabel(), actual.getLabel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByLabelShouldFailIfProductWithLabelIsMissing() {
        instock.findByLabel("no-such-label");
    }

    @Test
    public void testFindFirstByAlphabeticalOrderByNthNumberReturnsEmptyCollectionNotEnoughProducts() {
        int size = addMultipleProducts().size();
        List<Product> actual = iterableToList(instock.findFirstByAlphabeticalOrder(size + 1));
        assertEquals(0, actual.size());
    }

    @Test
    public void testFindFirstByAlphabeticalOrderByNthNumberReturnsEmptyCollectionCountIsZero() {
        addMultipleProducts();
        List<Product> actual = iterableToList(instock.findFirstByAlphabeticalOrder(0));
        assertEquals(0, actual.size());
    }

    @Test
    public void testFindFirstByAlphabeticalOrderByNthNumberReturnsProducts() {
        addMultipleProducts();

        int expectedCount = 3;
        List<Product> actual = iterableToList(instock.findFirstByAlphabeticalOrder(expectedCount));
        assertEquals(expectedCount, actual.size());
    }

    @Test
    public void testFindFirstByAlphabeticalOrderByNthNumberReturnsProductsOrderedByLabel() {
        int expectedCount = 3;

        List<Product> expected = addMultipleProducts()
                .stream()
                .sorted()
                .limit(expectedCount)
                .collect(Collectors.toList());

        List<Product> actual = iterableToList(instock.findFirstByAlphabeticalOrder(expectedCount));

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void testFindAllInRangeByPriceReturnsProducts() {
        double beginPrice = 3;
        double endPrice = 10;

        List<Product> expected = addMultipleProducts().stream()
                .filter(p -> p.getPrice() > beginPrice && p.getPrice() <= endPrice)
                .toList();

        List<Product> actual = iterableToList(instock.findAllInRange(beginPrice, endPrice));

        assertEquals(expected.size(), actual.size());

        boolean hasNoOutOfRangePrices = actual.stream()
                .map(Product::getPrice)
                .noneMatch(p -> p <= beginPrice || p > endPrice);

        assertTrue(hasNoOutOfRangePrices);
    }

    @Test
    public void testFindAllInRangeByPriceReturnsProductsOrderedByPriceDescending() {
        double beginPrice = 3;
        double endPrice = 10;

        List<Product> expected = addMultipleProducts().stream()
                .filter(p -> p.getPrice() > beginPrice && p.getPrice() <= endPrice)
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .toList();

        List<Product> actual = iterableToList(instock.findAllInRange(beginPrice, endPrice));

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            double expectedPrice = expected.get(i).getPrice();
            double actualPrice = actual.get(i).getPrice();

            assertEquals(expectedPrice, actualPrice, 0.00);
        }

        assertEquals(expected, actual);
    }

    @Test
    public void testFindAllByPriceShouldReturnMatchingPriceProducts() {
        double expectedPrice = 7;

        List<Product> expected = addMultipleProducts().stream()
                .filter(p -> p.getPrice() == expectedPrice)
                .toList();

        List<Product> actual = iterableToList(instock.findAllByPrice(expectedPrice));

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void testFindFirstMostExpensiveProductsShouldReturnCorrectNumberOfProductsWithHighestPrice() {
        int expectedAmount = 2;

        List<Product> expected = addMultipleProducts().stream()
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .limit(expectedAmount)
                .toList();

        List<Product> actual = iterableToList(instock.findFirstMostExpensiveProducts(expectedAmount));

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            double expectedPrice = expected.get(i).getPrice();
            double actualPrice = actual.get(i).getPrice();

            assertEquals(expectedPrice, actualPrice, 0.00);
        }

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindFirstMostExpensiveProductsShouldThrowExceptionIfNotEnoughProducts() {
        instock.findFirstMostExpensiveProducts(addMultipleProducts().size() + 1);
    }

    @Test
    public void testFindAllByQuantityShouldReturnMatchingQuantityProducts() {
        int expectedQuantity = 5;

        List<Product> expected = addMultipleProducts().stream()
                .filter(p -> p.getQuantity() == expectedQuantity)
                .toList();

        List<Product> actual = iterableToList(instock.findAllByQuantity(expectedQuantity));

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetIterableShouldReturnAllProductsInStock() {
        List<Product> expected = addMultipleProducts();
        List<Product> actual = new ArrayList<>();

        Iterator<Product> iterator = instock.iterator();

        assertNotNull(iterator);

        iterator.forEachRemaining(actual::add);

        assertEquals(expected, actual);
    }

    private List<Product> iterableToList(Iterable<Product> iterable) {
        assertNotNull(iterable);

        List<Product> products = new ArrayList<>();
        iterable.forEach(products::add);

        return products;
    }

    private List<Product> addMultipleProducts() {
        List<Product> products = List.of(
                new Product("label3", 7, 5),
                new Product("label2", 11, 2),
                new Product("label4", 8, 5),
                new Product("label6", 11, 9),
                new Product("label7", 7, 12),
                new Product("label1", 11, 5),
                new Product("label5", 2, 3)
        );

        products.forEach(instock::add);

        return products;
    }

}