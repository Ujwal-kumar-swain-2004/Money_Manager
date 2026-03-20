package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.FriendsDTO;
import in.bushansirgur.moneymanager.service.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/friends")
public class FriendsController {
    @Autowired
    private FriendsService friendsService;

    @GetMapping("/dashboard")
    public FriendsDTO dashboard() {
        return friendsService.dashboard();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FriendsDTO.FriendDTO addFriend(@RequestBody FriendsDTO.FriendDTO dto) {
        return friendsService.addFriend(dto);
    }

    @PatchMapping("/{friendId}/status")
    public FriendsDTO.FriendDTO updateStatus(@PathVariable Long friendId, @RequestBody Map<String, String> body) {
        return friendsService.updateFriendStatus(friendId, body.getOrDefault("status", "active"));
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendsDTO.FriendGroupDTO addGroup(@RequestBody FriendsDTO.FriendGroupDTO dto) {
        return friendsService.addGroup(dto);
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendsDTO.SharedExpenseDTO addExpense(@RequestBody FriendsDTO.SharedExpenseDTO dto) {
        return friendsService.addExpense(dto);
    }

    @PostMapping("/settlements")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendsDTO.SettlementDTO addSettlement(@RequestBody FriendsDTO.SettlementDTO dto) {
        return friendsService.addSettlement(dto);
    }

    @PostMapping("/reminders")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendsDTO.ReminderDTO addReminder(@RequestBody FriendsDTO.ReminderDTO dto) {
        return friendsService.addReminder(dto);
    }

    @PostMapping("/expenses/{expenseId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendsDTO.CommentDTO addComment(@PathVariable Long expenseId, @RequestBody FriendsDTO.CommentDTO dto) {
        return friendsService.addComment(expenseId, dto);
    }
}
