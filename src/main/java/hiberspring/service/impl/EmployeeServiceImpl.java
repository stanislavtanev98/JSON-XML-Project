package hiberspring.service.impl;

import hiberspring.common.GlobalConstants;
import hiberspring.domain.dtos.EmployeeRootSeedDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Employee;
import hiberspring.domain.entities.EmployeeCard;
import hiberspring.repository.EmployeeRepository;
import hiberspring.service.BranchService;
import hiberspring.service.EmployeeCardService;
import hiberspring.service.EmployeeService;
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
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;
    private final ValidationUtil validationUtil;
    private final EmployeeCardService employeeCardService;
    private final BranchService branchService;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, ModelMapper modelMapper, XmlParser xmlParser, ValidationUtil validationUtil, EmployeeCardService employeeCardService, BranchService branchService) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
        this.employeeCardService = employeeCardService;
        this.branchService = branchService;
    }

    @Override
    public Boolean employeesAreImported() {
        return this.employeeRepository.count() > 0;
    }

    @Override
    public String readEmployeesXmlFile() throws IOException {
        String content = Files.readString(Path.of(GlobalConstants.EMPLOYEES_FILE_PATH));
        return content;
    }

    @Override
    public String importEmployees() throws JAXBException, FileNotFoundException {
        StringBuilder output = new StringBuilder();
        EmployeeRootSeedDto employeeDtos = this.xmlParser
                .importFromXML(EmployeeRootSeedDto.class, GlobalConstants.EMPLOYEES_FILE_PATH);

        employeeDtos.getEmployees()
                .forEach(employeeDto -> {
                    if(this.validationUtil.isValid(employeeDto)){
                        if(this.branchService.getBranchByName(employeeDto.getBranch()) != null
                        && this.employeeCardService.findByCardNumber(employeeDto.getCard()) != null){

                            Employee employee = this.modelMapper.map(employeeDto, Employee.class);
                            Branch branch = this.branchService.getBranchByName(employeeDto.getBranch());
                            EmployeeCard employeeCard = this.employeeCardService.findByCardNumber(employeeDto.getCard());

                            employee.setBranch(branch);
                            employee.setEmployeeCard(employeeCard);

                            this.employeeRepository.saveAndFlush(employee);
                            output.append(String.format("Successfully added employee: %s %s",
                                    employee.getFirstName(), employee.getLastName()));
                        } else{
                            output.append(GlobalConstants.INCORRECT_DATA_MESSAGE);
                        }
                    } else {
                        output.append(GlobalConstants.INCORRECT_DATA_MESSAGE);
                    }
                    output.append(System.lineSeparator());
                });


        return output.toString();
    }

    @Override
    public String exportProductiveEmployees() {
        StringBuilder output = new StringBuilder();
        List<Employee> employees = this.employeeRepository.findAllWhichBranchesHaveMoreThanOneProduct();

        employees
                .forEach(employee -> {
                    output.append(String.format("Name: %s\nPosition: %s\nCard Number: %s\n",
                            employee.getFirstName() + " " + employee.getLastName(),
                            employee.getPosition(),
                            employee.getEmployeeCard().getNumber()));
                    output.append("-------------------------\n");
                });

        return output.toString();
    }
}
