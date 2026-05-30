package com.graduation.smarty_commerce.ui.Controller;

import com.graduation.smarty_commerce.Service.CommentService;
import com.graduation.smarty_commerce.Service.impl.CommentServiceImpl;
import com.graduation.smarty_commerce.shared.dto.CommentDto;
import com.graduation.smarty_commerce.ui.Model.Request.CommentRequestModel;
import com.graduation.smarty_commerce.ui.Model.Response.CommentRest;
import com.graduation.smarty_commerce.ui.Model.Response.OperationStatusModel;
import com.graduation.smarty_commerce.ui.Model.Response.RequestOperationName;
import com.graduation.smarty_commerce.ui.Model.Response.RequestOperationStatus;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.List;

@RestController
@RequestMapping("/products/{productId}/comments")
public class CommentController {

    @Autowired
    private CommentServiceImpl commentService;

    @GetMapping
    public List<CommentRest> getProductComments(@PathVariable String productId) {
        List<CommentDto> comments = commentService.getProductComments(productId);

        Type listType = new TypeToken<List<CommentRest>>() {}.getType();
        return new ModelMapper().map(comments, listType);
    }

    @PostMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    public ResponseEntity<CommentRest> createComment(@PathVariable String productId,
                                                     @PathVariable String userId,
                                                     @RequestBody CommentRequestModel commentDetails) {
        ModelMapper modelMapper = new ModelMapper();
        CommentDto commentDto = modelMapper.map(commentDetails, CommentDto.class);
        CommentDto createdComment = commentService.createComment(productId, userId, commentDto);
        CommentRest returnValue = modelMapper.map(createdComment, CommentRest.class);

        return new ResponseEntity<>(returnValue, HttpStatus.CREATED);
    }

    @PutMapping("/{commentId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    public CommentRest updateComment(@PathVariable String productId,
                                     @PathVariable String commentId,
                                     @PathVariable String userId,
                                     @RequestBody CommentRequestModel commentDetails) {
        ModelMapper modelMapper = new ModelMapper();
        CommentDto commentDto = modelMapper.map(commentDetails, CommentDto.class);
        CommentDto updateComment = commentService.updateComment(commentId, userId, commentDto);
        return modelMapper.map(updateComment, CommentRest.class);
    }

    @DeleteMapping("/{commentId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    public OperationStatusModel deleteComment(@PathVariable String productId,
                                              @PathVariable String commentId,
                                              @PathVariable String userId) {
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        commentService.deleteComment(commentId, userId);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        return returnValue;
    }
}
