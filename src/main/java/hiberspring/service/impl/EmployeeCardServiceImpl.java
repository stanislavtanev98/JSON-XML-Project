package hiberspring.service.impl;

import com.google.gson.Gson;
import hiberspring.common.GlobalConstants;
import hiberspring.domain.dtos.EmployeeCardSeedDto;
import hiberspring.domain.entities.Employee;
import hiberspring.domain.entities.EmployeeCard;
import hiberspring.repository.EmployeeCardRepository;
import hiberspring.service.EmployeeCardService;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class EmployeeCardServiceImpl implements EmployeeCardService {

    private final EmployeeCardRepository employeeCardRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final Gson gson;

    @Autowired
    public EmployeeCardServiceImpl(EmployeeCardRepository employeeCardRepository, ModelMapper modelMapper, ValidationUtil validationUtil, Gson gson) {
        this.employeeCardRepository = employeeCardRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
    }

    @Override
    public Boolean employeeCardsAreImported() {
        return this.employeeCardRepository.count() > 0;
    }

    @Override
    public String readEmployeeCardsJsonFile() throws IOException {

        String json = Files.readString(Path.of(GlobalConstants.EMPLOYEE_CARDS_FILE_PATH));
        return json;
    }

    @Override
    public String importEmployeeCards(String employeeCardsFileContent) throws FileNotFoundException {
        StringBuilder output = new StringBuilder();
        EmployeeCardSeedDto[] cardDtos = gson
                .fromJson(new FileReader(GlobalConstants.EMPLOYEE_CARDS_FILE_PATH), EmployeeCardSeedDto[].class);

        Arrays.stream(cardDtos)
                .forEach(cardDto -> {
                    if(this.validationUtil.isValid(cardDto)){
                        if(this.employeeCardRepository.findByNumber(cardDto.getNumber()) == null) {
                            EmployeeCard card = this.modelMapper.map(cardDto, EmployeeCard.class);

                            this.employeeCardRepository.saveAndFlush(card);

                            output.append(String.format("Successfully added card - %s", card.getNumber()));
                        } else {
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
    public EmployeeCard findByCardNumber(String number) {
        return this.employeeCardRepository.findByNumber(number);
    }
}
