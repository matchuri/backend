package matchuri.backend.api.menu;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping
    public String getAdminHome() {
        return "redirect:/admin/menu-items";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "admin/login";
    }
}
