package com.uef.library.controller;

import com.uef.library.service.ChatPromptService;
import com.uef.library.service.TrainAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/persona-management")
@RequiredArgsConstructor
public class TrainAiController {

    private final TrainAiService trainAiService;
    private final ChatPromptService chatPromptService;

    @GetMapping
    public String showTrainAiPage(Model model) {
        try {
            model.addAttribute("personas", trainAiService.getAllPersonas());
            model.addAttribute("activePersona", trainAiService.getActivePersonaName());
            model.addAttribute("customPersonaTemplate", trainAiService.getCustomPersonaTemplate());
            return "admin/train-ai";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi khi tải dữ liệu: " + e.getMessage());
            return "admin/train-ai";
        }
    }

    @PostMapping("/activate")
    public String activatePersona(@RequestParam String personaName,
                                  RedirectAttributes redirectAttributes) {
        try {
            trainAiService.setActivePersona(personaName);
            chatPromptService.reloadKnowledge();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã kích hoạt thành công persona: " + getPersonaDisplayName(personaName));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi khi kích hoạt persona: " + e.getMessage());
        }
        return "redirect:/admin/persona-management";
    }

    @PostMapping("/create")
    public String createPersona(@RequestParam String displayName,
                                @RequestParam(required = false) String description,
                                @RequestParam String knowledge,
                                RedirectAttributes redirectAttributes) {
        try {
            trainAiService.createNewPersona(displayName, description, knowledge);
            chatPromptService.reloadKnowledge();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã tạo và kích hoạt thành công persona: " + displayName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi khi tạo persona: " + e.getMessage());
        }
        return "redirect:/admin/persona-management";
    }

    @PostMapping("/delete")
    public String deletePersona(@RequestParam String personaName,
                                RedirectAttributes redirectAttributes) {
        try {
            // Get display name before deletion for message
            String displayName = trainAiService.getAllPersonas().stream()
                    .filter(p -> p.getName().equals(personaName))
                    .findFirst()
                    .map(p -> p.getDisplayName())
                    .orElse(personaName);

            trainAiService.deleteCustomPersona(personaName);
            chatPromptService.reloadKnowledge();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa thành công persona: " + displayName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi khi xóa persona: " + e.getMessage());
        }
        return "redirect:/admin/persona-management";
    }

    private String getPersonaDisplayName(String personaName) {
        return trainAiService.getAllPersonas().stream()
                .filter(p -> p.getName().equals(personaName))
                .findFirst()
                .map(p -> p.getDisplayName())
                .orElse(personaName);
    }
}