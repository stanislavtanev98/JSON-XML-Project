package hiberspring.service.impl;

import com.google.gson.Gson;
import hiberspring.common.GlobalConstants;
import hiberspring.domain.dtos.BranchSeedDto;
import hiberspring.domain.entities.Branch;
import hiberspring.domain.entities.Town;
import hiberspring.repository.BranchRepository;
import hiberspring.service.BranchService;
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
import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final TownService townService;

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository, ModelMapper modelMapper, Gson gson, ValidationUtil validationUtil, TownService townService) {
        this.branchRepository = branchRepository;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.townService = townService;
    }

    @Override
    public Boolean branchesAreImported() {
        return this.branchRepository.count() > 0;
    }

    @Override
    public String readBranchesJsonFile() throws IOException {

        String json = Files.readString(Path.of(GlobalConstants.BRANCHES_FILE_PATH));
        return json;
    }

    @Override
    public String importBranches(String branchesFileContent) throws FileNotFoundException {
        StringBuilder message = new StringBuilder();
        BranchSeedDto[] branchDtos = gson.fromJson(new FileReader(GlobalConstants.BRANCHES_FILE_PATH), BranchSeedDto[].class);

        Arrays.stream(branchDtos)
                .forEach(branchSeedDto -> {
                    if(this.validationUtil.isValid(branchSeedDto)){
                        if(townService.findByName(branchSeedDto.getTown()) != null){
                            Town town = townService.findByName(branchSeedDto.getTown());
                            Branch branch = this.modelMapper.map(branchSeedDto, Branch.class);
                            branch.setTown(town);

                            this.branchRepository.saveAndFlush(branch);

                            message.append(String.format("Successfully added %s branch.", branch.getName()));
                        } else {
                            message.append(GlobalConstants.INCORRECT_DATA_MESSAGE);
                        }
                    } else {
                        message.append(GlobalConstants.INCORRECT_DATA_MESSAGE);
                    }
                    message.append(System.lineSeparator());
                });

        return message.toString();
    }

    @Override
    public Branch getBranchByName(String name) {
        return this.branchRepository.getByName(name);
    }

    @Override
    public List<Branch> allBranchesWithProducts() {
        return this.branchRepository.findAllByBranchesWithProducts();
    }
}
