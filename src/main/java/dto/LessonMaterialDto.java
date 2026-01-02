package dto;

import entity.lesson.LessonMaterial;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonMaterialDto {
    private Long id;
    private LessonMaterial.MaterialType materialType;
    private String title;
    private String content;
    private Integer sortOrder;

    public static LessonMaterialDto fromEntity(LessonMaterial material) {
        if (material == null) return null;

        return LessonMaterialDto.builder()
                .id(material.getId())
                .materialType(material.getMaterialType())
                .title(material.getTitle())
                .content(material.getContent())
                .sortOrder(material.getSortOrder())
                .build();
    }

    public LessonMaterial toEntity() {
        return LessonMaterial.builder()
                .id(this.id)
                .materialType(this.materialType)
                .title(this.title)
                .content(this.content)
                .sortOrder(this.sortOrder != null ? this.sortOrder : 0)
                .build();
    }
}
