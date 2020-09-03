package hiberspring.service.impl;

import hiberspring.common.GlobalConstants;
import hiberspring.domain.dtos.ProductRootSeedDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Product;
import hiberspring.repository.ProductRepository;
import hiberspring.service.BranchService;
import hiberspring.service.ProductService;
import hiberspring.util.ValidationUtil;
import hiberspring.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final BranchService branchService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser, BranchService branchService) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.branchService = branchService;
    }

    @Override
    public Boolean productsAreImported() {
        return this.productRepository.count() > 0;
    }

    @Override
    public String readProductsXmlFile() throws IOException {
        String content = Files.readString(Path.of(GlobalConstants.PRODUCTS_FILE_PATH));

        return content;
    }

    @Override
    public String importProducts() throws JAXBException, FileNotFoundException {
        StringBuilder output = new StringBuilder();

        ProductRootSeedDto productRootSeedDto = this.xmlParser.importFromXML(ProductRootSeedDto.class, GlobalConstants.PRODUCTS_FILE_PATH);

        productRootSeedDto.getProducts()
                .forEach(productDto -> {
                    if(this.validationUtil.isValid(productDto)){
                        if(this.branchService.getBranchByName(productDto.getBranch()) != null){
                            Product product = this.modelMapper.map(productDto, Product.class);
                            Branch branch = this.branchService.getBranchByName(productDto.getBranch());

                            product.setBranch(branch);

                            this.productRepository.saveAndFlush(product);

                            output.append(String.format("Successfully added product: %s", product.getName()));
                        } else {
                            output.append(GlobalConstants.INCORRECT_DATA_MESSAGE);
                        }
                    } else{
                        output.append(GlobalConstants.INCORRECT_DATA_MESSAGE);
                    }
                    output.append(System.lineSeparator());
                });

        return output.toString();
    }
}
