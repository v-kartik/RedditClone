package com.kartik.Reddit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentsDto {

    private Long id;
    private Long postId;
    private Instant createDate;
    private String text;
    private String username;

}
