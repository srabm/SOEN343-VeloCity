package controller;

import com.concordia.velocity.backend.model.Dock;
import com.concordia.velocity.backend.service.DockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/docks")
public class DockController {

    private final DockService dockService;

    public DockController(DockService dockService) {
        this.dockService = dockService;
    }

    @PostMapping
    public String createDock(@RequestBody Dock dock) throws ExecutionException, InterruptedException {
        return dockService.saveDock(dock);
    }

    @GetMapping("/{dockId}")
    public Dock getDock(@PathVariable String dockId) throws ExecutionException, InterruptedException {
        return dockService.getDockById(dockId);
    }

    @GetMapping
    public List<Dock> getAllDocks() throws ExecutionException, InterruptedException {
        return dockService.getAllDocks();
    }

    @DeleteMapping("/{dockId}")
    public String deleteDock(@PathVariable String dockId) throws ExecutionException, InterruptedException {
        return dockService.deleteDock(dockId);
    }

    @PutMapping("/{dockId}/status")
    public String updateDockStatus(
            @PathVariable String dockId,
            @RequestParam String status
    ) throws ExecutionException, InterruptedException {
        return dockService.updateDockStatus(dockId, status);
    }
}
