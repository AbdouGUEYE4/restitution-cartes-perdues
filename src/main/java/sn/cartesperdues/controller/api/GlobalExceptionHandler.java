package sn.cartesperdues.controller.api;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Gérer les erreurs de fichier trop volumineux
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error",
                "Le fichier est trop volumineux. Taille maximum: 5MB");
        return "redirect:/publier";
    }

    // Gérer les erreurs générales
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        model.addAttribute("error", "Une erreur est survenue: " + e.getMessage());
        return "error";
    }
}