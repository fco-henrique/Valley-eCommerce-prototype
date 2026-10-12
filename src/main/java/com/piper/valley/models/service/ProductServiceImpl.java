package com.piper.valley.models.service;

import com.piper.valley.forms.AddProductForm;
import com.piper.valley.models.domain.Product;
import com.piper.valley.models.factory.ProductCreator;
import com.piper.valley.models.repository.BrandRepository;
import com.piper.valley.models.repository.CompanyRepository;
import com.piper.valley.models.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final BrandRepository brandRepository;
	private final CompanyRepository companyRepository;
	private final ProductCreator physicalProductCreator;
	private final ProductCreator virtualProductCreator;

	@Autowired
	public ProductServiceImpl(ProductRepository productRepository,
			BrandRepository brandRepository, CompanyRepository companyRepository,
			@Qualifier("physicalProductCreator") ProductCreator physicalProductCreator,
			@Qualifier("virtualProductCreator") ProductCreator virtualProductCreator) {
		this.productRepository = productRepository;
		this.brandRepository = brandRepository;
		this.companyRepository = companyRepository;
		this.physicalProductCreator = physicalProductCreator;
		this.virtualProductCreator = virtualProductCreator;
	}

	@Override
	public Optional<Product> getProductById(Long id) {
		return Optional.ofNullable(productRepository.findOne(id));
	}

	@Override
	public Optional<Product> getProductByName(String name) {
		return productRepository.findByName(name);
	}

	@Override
	public Optional<Product> getPriceBetween(Float start,Float end)
	{
		return productRepository.findByAveragePriceBetween(start,end);
	}

	@Override
	public Collection<Product>getAllProducts()
	{
		return productRepository.findAll();
	}

	@Override
	public Product addProduct(AddProductForm productForm) {
		ProductCreator creator = productForm.getPhysicalProduct()
				? physicalProductCreator : virtualProductCreator;
		Product product = creator.createProduct(productForm);

		//Common Attributes
		product.setBrand(brandRepository.findOneById(productForm.getBrandId()).get());
		product.setCompany(companyRepository.findOneById(productForm.getCompanyId()).get());
		product.setName(productForm.getName());
		product.setAveragePrice(productForm.getAveragePrice());
		product.setDateTime(new Date());

		return productRepository.save(product);
	}
	public void incrementViews(Product product) {
			product.setView(product.getView()+1);
			productRepository.save(product);
	}
}
