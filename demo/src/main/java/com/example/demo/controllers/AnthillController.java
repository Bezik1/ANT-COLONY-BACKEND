package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Ant;
import com.example.demo.model.Anthill;
import com.example.demo.types.Cells.AirCell;
import com.example.demo.types.Cells.Cell;
import com.example.demo.types.Cells.ColonyCell;
import com.example.demo.types.Cells.FoodCell;
import com.example.demo.types.Cells.WallCell;
import com.example.demo.types.Users.Vector;
import com.example.demo.repository.AntRepository;
import com.example.demo.repository.AnthillRepository;

import java.util.List;
import java.util.Optional;

class SimulationRequest {
    private int steps;

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }
}

@RestController
@RequestMapping("/colony")
public class AnthillController {

    @Autowired
    private AnthillRepository anthillRepository;

    @Autowired
    private AntRepository antRepository;

    @DeleteMapping("/{anthillId}/removeAnthill")
    public void removeAnhtill(@PathVariable String anthillId) {
        Optional<Anthill> anthillOptional = anthillRepository.findById(anthillId);
        if (anthillOptional.isEmpty()) {
            throw new IllegalArgumentException("Anthill not found with ID: " + anthillId);
        }
        Anthill anthill = anthillOptional.get();
        anthillRepository.delete(anthill);
    }

    @PostMapping
    public Anthill addAnthill() {
        Anthill anthill = new Anthill();
        anthillRepository.save(anthill);
        return anthill;
    }

    @GetMapping("/{anthillId}/getAnts")
    public List<Ant> getAnthillAnts(@PathVariable String anthillId) {
        Optional<Anthill> anthillOptional = anthillRepository.findById(anthillId);
        if (anthillOptional.isEmpty()) {
            throw new IllegalArgumentException("Anthill not found with ID: " + anthillId);
        }

        List<Ant> antList = antRepository.findByAnthillId(anthillId);
        return antList;
    }

    @PostMapping("/{anthillId}/addAnt")
    public Ant addAntToAnthill(@PathVariable String anthillId) {
        Optional<Anthill> anthillOptional = anthillRepository.findById(anthillId);
        if (anthillOptional.isEmpty()) {
            throw new IllegalArgumentException("Anthill not found with ID: " + anthillId);
        }
        Anthill anthill = anthillOptional.get();


        Vector colonyPosition = anthill.findColonyCellCoordinates();
        Ant ant = new Ant(anthillId, colonyPosition);
        Ant savedAnt = antRepository.save(ant);
        return savedAnt;
    }

    @GetMapping("/anthills")
    public List<Anthill> getAnthills() {
        List<Anthill> anthillList = anthillRepository.findAll();
        return anthillList;
    }

    @GetMapping("/{anthillId}/grid")
    public Cell[][][] getGrid(@PathVariable String anthillId) {
        Optional<Anthill> anthillOptional = anthillRepository.findById(anthillId);
        if (anthillOptional.isEmpty()) {
            throw new IllegalArgumentException("Anthill not found with ID: " + anthillId);
        }

        Anthill anthill = anthillOptional.get();
        return anthill.getGrid();
    }

    @GetMapping("/{anthillId}/gridText")
    public String getGridText(@PathVariable String anthillId) {
        Optional<Anthill> anthillOptional = anthillRepository.findById(anthillId);
        if (anthillOptional.isEmpty()) {
            throw new IllegalArgumentException("Anthill not found with ID: " + anthillId);
        }

        Anthill anthill = anthillOptional.get();
        StringBuilder gridRepresentation = new StringBuilder();

        List<Ant> ants = antRepository.findByAnthillId(anthillId);

        for (int y = 0; y < anthill.getGrid()[0].length; y++) {
            gridRepresentation.append("Layer Z=").append(y).append("\n");
            for (int z = 0; z < anthill.getGrid()[0][0].length; z++) {
                for (int x = 0; x < anthill.getGrid().length; x++) {
                    Cell cell = anthill.getGrid()[x][y][z];
                    char cellRepresentation = ' ';

                    if (cell instanceof WallCell) {
                        cellRepresentation = '#';
                    } else if (cell instanceof FoodCell) {
                        cellRepresentation = 'F';
                    } else if (cell instanceof ColonyCell) {
                        cellRepresentation = 'C';
                    }else if(cell instanceof AirCell) {
                        AirCell airCell = (AirCell) cell;

                        if(airCell.getPassableForAnt()) {
                            cellRepresentation = '.';
                        } else {
                            cellRepresentation = 'X';
                        }
                    }
                    for (Ant ant : ants) {
                        if (ant.getPosition().equals(new Vector(x, y, z))) {
                            cellRepresentation = 'A';
                            break;
                        }
                    }

                    gridRepresentation.append(cellRepresentation).append(" ");
                }
                gridRepresentation.append("\n");
            }
            gridRepresentation.append("\n");
        }
        return gridRepresentation.toString();
    }

    @GetMapping("/{anthillId}/getColonyPosition")
    public Vector getColonyPosition(@PathVariable String anthillId) {
        Optional<Anthill> anthillOptional = anthillRepository.findById(anthillId);
        if (anthillOptional.isEmpty()) {
            throw new IllegalArgumentException("Anthill not found with ID: " + anthillId);
        }

        Anthill anthill = anthillOptional.get();
        return anthill.findColonyCellCoordinates();
    }

    @PostMapping("/{anthillId}/simulate")
    public String simulateAnthill(@PathVariable String anthillId, @RequestBody SimulationRequest stepRequest) {
        try {
            Optional<Anthill> anthillOptional = anthillRepository.findById(anthillId);
            if (anthillOptional.isEmpty()) {
                throw new IllegalArgumentException("Anthill not found with ID: " + anthillId);
            }

            Anthill anthill = anthillOptional.get();
            List<Ant> ants = antRepository.findByAnthillId(anthillId);

            for (int i = 0; i < stepRequest.getSteps(); i++) {
                ants.parallelStream().forEach(ant -> {
                    ant.move(anthill, anthillRepository, antRepository);

                    // if(ant.getExpirationTime() == 0) {
                    //     antRepository.delete(ant);
                    // } else {
                        antRepository.save(ant);
                    //}
                });

                anthill.evaporatePheromones();
                Anthill anthillSaved = anthillRepository.save(anthill);
            }

            return "Simulation completed for " + stepRequest.getSteps() + " steps.";
        } catch(Exception err) {
            System.out.println(err);
            return err.getMessage();
        }
    }
}
