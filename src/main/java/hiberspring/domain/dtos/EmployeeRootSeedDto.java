package hiberspring.domain.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "employees")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployeeRootSeedDto {

    @XmlElement(name = "employee")
    private List<EmployeeSeedDto> employees;

    public EmployeeRootSeedDto(){
    }

    public List<EmployeeSeedDto> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeSeedDto> employees) {
        this.employees = employees;
    }
}
