package com.piper.valley.models.service;

import com.piper.valley.forms.AddProductForm;
import com.piper.valley.models.domain.Brand;
import com.piper.valley.models.domain.Company;
import com.piper.valley.models.domain.PhysicalProduct;
import com.piper.valley.models.domain.Product;
import com.piper.valley.models.domain.VirtualProduct;
import com.piper.valley.models.factory.PhysicalProductCreator;
import com.piper.valley.models.factory.VirtualProductCreator;
import com.piper.valley.models.repository.BrandRepository;
import com.piper.valley.models.repository.CompanyRepository;
import com.piper.valley.models.repository.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/** Characterizes the legacy creation contract, including failure ordering. */
public class ProductServiceRefactoringTest {
    private ProductRepository products;
    private BrandRepository brands;
    private CompanyRepository companies;
    private ProductServiceImpl service;
    private AddProductForm form;
    private Brand brand;
    private Company company;

    @Before
    public void setUp() {
        products = mock(ProductRepository.class);
        brands = mock(BrandRepository.class);
        companies = mock(CompanyRepository.class);
        service = new ProductServiceImpl(products, brands, companies,
                new PhysicalProductCreator(), new VirtualProductCreator());
        brand = new Brand();
        company = new Company();
        when(brands.findOneById(1)).thenReturn(Optional.of(brand));
        when(companies.findOneById(2)).thenReturn(Optional.of(company));
        when(products.save(any(Product.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        form = new AddProductForm();
        form.setBrandId(1);
        form.setCompanyId(2);
        form.setName("Example product");
        form.setAveragePrice(25.5f);
        form.setLength(1f);
        form.setWidth(2f);
        form.setHeight(3f);
        form.setWeight(4f);
        form.setSerial("SERIAL-123");
    }

    @Test
    public void createsPhysicalProductWithAllFieldsAndSavesOnce() {
        long before = System.currentTimeMillis();
        Product result = service.addProduct(form);
        assertTrue(result instanceof PhysicalProduct);
        PhysicalProduct physical = (PhysicalProduct) result;
        assertEquals(1f, physical.getLength(), 0f);
        assertEquals(2f, physical.getWidth(), 0f);
        assertEquals(3f, physical.getHeight(), 0f);
        assertEquals(4f, physical.getWeight(), 0f);
        assertCommonFieldsAndSaveOrder(result, before);
    }

    @Test
    public void createsVirtualProductWithoutRequiringPhysicalFields() {
        form.setPhysicalProduct(false);
        form.setLength(null);
        form.setWidth(null);
        form.setHeight(null);
        form.setWeight(null);
        long before = System.currentTimeMillis();
        Product result = service.addProduct(form);
        assertTrue(result instanceof VirtualProduct);
        assertEquals("SERIAL-123", ((VirtualProduct) result).getSerial());
        assertCommonFieldsAndSaveOrder(result, before);
    }

    @Test
    public void returnsRepositoryResult() {
        Product saved = new VirtualProduct();
        when(products.save(any(Product.class))).thenReturn(saved);
        assertSame(saved, service.addProduct(form));
    }

    @Test
    public void missingBrandFailsBeforeCompanyLookupOrSave() {
        when(brands.findOneById(1)).thenReturn(Optional.<Brand>empty());
        try {
            service.addProduct(form);
            fail("Expected the legacy Optional.get failure");
        } catch (NoSuchElementException expected) {
            verifyZeroInteractions(companies, products);
        }
    }

    @Test
    public void missingCompanyDoesNotSave() {
        when(companies.findOneById(2)).thenReturn(Optional.<Company>empty());
        try {
            service.addProduct(form);
            fail("Expected the legacy Optional.get failure");
        } catch (NoSuchElementException expected) {
            InOrder order = inOrder(brands, companies);
            order.verify(brands).findOneById(1);
            order.verify(companies).findOneById(2);
            verifyZeroInteractions(products);
        }
    }

    @Test
    public void nullPhysicalFlagFailsBeforeRepositoryAccess() {
        form.setPhysicalProduct(null);
        assertNullUnboxingFailure();
    }

    @Test
    public void nullPhysicalDimensionFailsBeforeRepositoryAccess() {
        form.setLength(null);
        assertNullUnboxingFailure();
    }

    private void assertNullUnboxingFailure() {
        try {
            service.addProduct(form);
            fail("Expected the legacy null-unboxing failure");
        } catch (NullPointerException expected) {
            verifyZeroInteractions(brands, companies, products);
        }
    }

    private void assertCommonFieldsAndSaveOrder(Product product, long before) {
        assertSame(brand, product.getBrand());
        assertSame(company, product.getCompany());
        assertEquals("Example product", product.getName());
        assertEquals(25.5f, product.getAveragePrice(), 0f);
        assertEquals(0, product.getView());
        assertTrue(product.getDateTime().getTime() >= before);
        assertTrue(product.getDateTime().getTime() <= System.currentTimeMillis());
        InOrder order = inOrder(brands, companies, products);
        order.verify(brands).findOneById(1);
        order.verify(companies).findOneById(2);
        order.verify(products).save(product);
        verifyNoMoreInteractions(brands, companies, products);
    }
}
