package com.example.campusactivity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询基础DTO
 * 所有分页查询请求继承此类
 */
@Data
@Schema(description = "分页查询参数")
public class PageQueryDTO {

    /**
     * 当前页码（从1开始）
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词")
    private String keyword;
}
