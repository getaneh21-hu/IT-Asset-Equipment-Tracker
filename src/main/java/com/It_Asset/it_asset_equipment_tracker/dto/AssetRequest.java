
package com.It_Asset.it_asset_equipment_tracker.dto;

import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AssetRequest {

    @NotBlank(message = "Asset name is required")
    @Size(
        min = 2,
        max = 100,
        message = "Asset name must be between 2 and 100 characters"
    )
    @Pattern(
        regexp = ".*[A-Za-z].*",
        message = "Asset name must contain at least one English letter"
    )
    private String name1;

    @NotBlank(message = "Asset type is required")
    @Size(
        max = 50,
        message = "Asset type must not exceed 50 characters"
    )
    private String type;

    @NotBlank(message = "Serial number is required")
    @Size(
        max = 100,
        message = "Serial number must not exceed 100 characters"
    )
    private String serialNumber;

    @Size(
        max = 100,
        message = "Brand must not exceed 100 characters"
    )
    private String brand;

    @Size(
        max = 100,
        message = "Model must not exceed 100 characters"
    )
    private String model;

    @Size(
        max = 100,
        message = "Department must not exceed 100 characters"
    )
    private String department;

    // ID of the category assigned to this asset
    @NotNull(message = "Asset category is required")
     private Long categoryId;

    @NotNull(message = "Asset status is required")
    private AssetStatus status = AssetStatus.AVAILABLE;

    @NotNull(message = "Purchase cost is required")
    @DecimalMin(
        value = "0.0",
        inclusive = true,
        message = "Purchase cost cannot be negative"
    )
    private BigDecimal purchaseCost;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    // Warranty expiration date (optional)
    private LocalDate warrantyExpiryDate;

    // Default constructor
    public AssetRequest() {
    }

    // Existing service compatibility
    public String getName() {
        return name1;
    }

    public void setName(String name) {
        this.name1 = name;
    }

    // Explicit JSON property mapping for name1
    @JsonProperty("name1")
    public String getName1() {
        return name1;
    }

    @JsonProperty("name1")
    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }

    public BigDecimal getPurchaseCost() {
        return purchaseCost;
    }

    public void setPurchaseCost(BigDecimal purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getWarrantyExpiryDate() {
        return warrantyExpiryDate;
    }

    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) {
        this.warrantyExpiryDate = warrantyExpiryDate;
    }
}
