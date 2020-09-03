package hiberspring.service.impl;

import com.google.gson.Gson;
import hiberspring.common.GlobalConstants;
import hiberspring.domain.dtos.TownSeedDto;
import hiberspring.domain.entities.Town;
import hiberspring.repository.TownRepository;
import hiberspring.service.TownService;
import hiberspring.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class TownsServiceImpl implements TownService {

    private final TownRepository townRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtils;
    private final Gson gson;

    @Autowired
    public TownsServiceImpl(TownRepository townRepository, ModelMapper modelMapper, ValidationUtil validationUtils, Gson gson) {
        this.townRepository = townRepository;
        this.modelMapper = modelMapper;
        this.validationUtils = validationUtils;
        this.gson = gson;
    }

    @Override
    public Boolean townsAreImported() {
        return this.townRepository.count() > 0;
    }

    @Override
    public String readTownsJsonFile() throws IOException {

        String json = Files.readString(Path.of(GlobalConstants.TOWNS_FILE_PATH));

        return json;
    }

    @Override
    public String importTowns(String townsFileContent) throws FileNotFoundException {
        TownSeedDto[] towns =  this.gson.fromJson(new FileReader(GlobalConstants.TOWNS_FILE_PATH), TownSeedDto[].class);
        StringBuilder message = new StringBuilder();

        Arrays.stream(towns)
                .forEach(town -> {
                    if(this.validationUtils.isValid(town)){
                        Town bigTown = this.modelMapper.map(town, Town.class);

                        this.townRepository.saveAndFlush(bigTown);
                        message.append(String.format("Successfully added %s!", town.getName()));
                    } else {
                        message.append(GlobalConstants.INCORRECT_DATA_MESSAGE);
                    }
                    message.append(System.lineSeparator());
                });

        return message.toString();
    }

    @Override
    public Town findByName(String name) {
        return this.townRepository.findByName(name);
    }
}
