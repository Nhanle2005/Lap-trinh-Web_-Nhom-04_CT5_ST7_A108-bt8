// Product Management JavaScript
$(document).ready(function() {
    let productsTable;
    let isEditMode = false;
    let currentProductId = null;

    // Initialize DataTable
    initializeDataTable();
    
    // Load categories for filter and form
    loadCategories();
    
    // Event handlers
    $('#btnAddProduct').on('click', showAddProductModal);
    $('#productForm').on('submit', handleProductFormSubmit);
    $('#categoryFilter, #statusFilter').on('change', filterProducts);
    $('#searchKeyword').on('input', debounce(filterProducts, 300));
    
    // Initialize DataTable
    function initializeDataTable() {
        productsTable = $('#productsTable').DataTable({
            processing: true,
            serverSide: false,
            ajax: {
                url: '/api/products',
                type: 'GET',
                data: function(d) {
                    return {
                        status: $('#statusFilter').val(),
                        categoryId: $('#categoryFilter').val() || null,
                        keyword: $('#searchKeyword').val() || null
                    };
                },
                dataSrc: function(json) {
                    return json;
                }
            },
            columns: [
                { data: 'productId', title: 'ID' },
                { 
                    data: 'image', 
                    title: 'Hình ảnh',
                    render: function(data, type, row) {
                        if (data && data.trim() !== '') {
                            return `<img src="${data}" alt="${row.productName}" style="width: 50px; height: 50px; object-fit: cover;">`;
                        }
                        return '<span class="text-muted">Không có</span>';
                    },
                    orderable: false
                },
                { data: 'productName', title: 'Tên sản phẩm' },
                { data: 'categoryName', title: 'Danh mục' },
                { 
                    data: 'price', 
                    title: 'Giá',
                    render: function(data, type, row) {
                        return new Intl.NumberFormat('vi-VN', {
                            style: 'currency',
                            currency: 'VND'
                        }).format(data);
                    }
                },
                { data: 'quantity', title: 'Số lượng' },
                { 
                    data: 'status', 
                    title: 'Trạng thái',
                    render: function(data, type, row) {
                        return data ? 
                            '<span class="badge bg-success">Hoạt động</span>' : 
                            '<span class="badge bg-danger">Đã xóa</span>';
                    }
                },
                { 
                    data: 'createdAt', 
                    title: 'Ngày tạo',
                    render: function(data, type, row) {
                        return data ? new Date(data).toLocaleString('vi-VN') : '';
                    }
                },
                {
                    data: null,
                    title: 'Thao tác',
                    render: function(data, type, row) {
                        let buttons = '';
                        
                        if (row.status) {
                            buttons += `
                                <button class="btn btn-sm btn-info me-1" onclick="viewProduct(${row.productId})" title="Xem">
                                    <i class="fas fa-eye"></i>
                                </button>
                                <button class="btn btn-sm btn-warning me-1" onclick="editProduct(${row.productId})" title="Sửa">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-danger" onclick="deleteProduct(${row.productId})" title="Xóa">
                                    <i class="fas fa-trash"></i>
                                </button>
                            `;
                        } else {
                            buttons += `
                                <button class="btn btn-sm btn-success me-1" onclick="restoreProduct(${row.productId})" title="Khôi phục">
                                    <i class="fas fa-undo"></i>
                                </button>
                                <button class="btn btn-sm btn-danger" onclick="permanentDeleteProduct(${row.productId})" title="Xóa vĩnh viễn">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            `;
                        }
                        
                        return buttons;
                    },
                    orderable: false
                }
            ],
            order: [[0, 'desc']],
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.4/i18n/vi.json'
            },
            responsive: true
        });
    }
    
    // Load categories for filter and form
    function loadCategories() {
        $.ajax({
            url: '/api/categories',
            type: 'GET',
            data: { status: true },
            success: function(categories) {
                // Load for filter
                const categoryFilter = $('#categoryFilter');
                categoryFilter.find('option:not(:first)').remove();
                
                // Load for form
                const categorySelect = $('#categoryId');
                categorySelect.find('option:not(:first)').remove();
                
                categories.forEach(function(category) {
                    categoryFilter.append(
                        `<option value="${category.categoryId}">${category.categoryName}</option>`
                    );
                    categorySelect.append(
                        `<option value="${category.categoryId}">${category.categoryName}</option>`
                    );
                });
            },
            error: function(xhr) {
                console.error('Error loading categories:', xhr.responseJSON);
                showAlert('error', 'Lỗi khi tải danh mục: ' + getErrorMessage(xhr));
            }
        });
    }
    
    // Filter products
    function filterProducts() {
        productsTable.ajax.reload();
    }
    
    // Show add product modal
    function showAddProductModal() {
        isEditMode = false;
        currentProductId = null;
        $('#productModalLabel').text('Thêm sản phẩm');
        $('#productForm')[0].reset();
        $('#status').prop('checked', true);
        clearValidationErrors();
        $('#productModal').modal('show');
    }
    
    // Handle product form submit
    function handleProductFormSubmit(e) {
        e.preventDefault();
        
        const formData = {
            productName: $('#productName').val(),
            categoryId: $('#categoryId').val(),
            price: parseFloat($('#price').val()),
            quantity: parseInt($('#quantity').val()),
            description: $('#description').val(),
            image: $('#image').val(),
            status: $('#status').is(':checked')
        };
        
        if (isEditMode) {
            formData.productId = currentProductId;
            updateProduct(formData);
        } else {
            createProduct(formData);
        }
    }
    
    // Create product
    function createProduct(formData) {
        $.ajax({
            url: '/api/products',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function(response) {
                $('#productModal').modal('hide');
                showAlert('success', 'Thêm sản phẩm thành công!');
                productsTable.ajax.reload();
            },
            error: function(xhr) {
                if (xhr.status === 400) {
                    displayValidationErrors(xhr.responseJSON);
                } else {
                    showAlert('error', 'Lỗi khi thêm sản phẩm: ' + getErrorMessage(xhr));
                }
            }
        });
    }
    
    // Update product
    function updateProduct(formData) {
        $.ajax({
            url: `/api/products/${currentProductId}`,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function(response) {
                $('#productModal').modal('hide');
                showAlert('success', 'Cập nhật sản phẩm thành công!');
                productsTable.ajax.reload();
            },
            error: function(xhr) {
                if (xhr.status === 400 || xhr.status === 422) {
                    displayValidationErrors(xhr.responseJSON);
                } else {
                    showAlert('error', 'Lỗi khi cập nhật sản phẩm: ' + getErrorMessage(xhr));
                }
            }
        });
    }
    
    // Edit product
    window.editProduct = function(productId) {
        $.ajax({
            url: `/api/products/${productId}`,
            type: 'GET',
            success: function(product) {
                isEditMode = true;
                currentProductId = productId;
                $('#productModalLabel').text('Sửa sản phẩm');
                
                $('#productName').val(product.productName);
                $('#categoryId').val(product.categoryId);
                $('#price').val(product.price);
                $('#quantity').val(product.quantity);
                $('#description').val(product.description || '');
                $('#image').val(product.image || '');
                $('#status').prop('checked', product.status);
                
                clearValidationErrors();
                $('#productModal').modal('show');
            },
            error: function(xhr) {
                showAlert('error', 'Lỗi khi tải thông tin sản phẩm: ' + getErrorMessage(xhr));
            }
        });
    };
    
    // View product
    window.viewProduct = function(productId) {
        $.ajax({
            url: `/api/products/${productId}`,
            type: 'GET',
            success: function(product) {
                let imageHtml = product.image ? 
                    `<img src="${product.image}" alt="${product.productName}" class="img-fluid" style="max-width: 200px;">` :
                    '<span class="text-muted">Không có hình ảnh</span>';
                
                let price = new Intl.NumberFormat('vi-VN', {
                    style: 'currency',
                    currency: 'VND'
                }).format(product.price);
                
                Swal.fire({
                    title: product.productName,
                    html: `
                        <div class="text-start">
                            <p><strong>Danh mục:</strong> ${product.categoryName}</p>
                            <p><strong>Giá:</strong> ${price}</p>
                            <p><strong>Số lượng:</strong> ${product.quantity}</p>
                            <p><strong>Mô tả:</strong> ${product.description || 'Không có'}</p>
                            <p><strong>Trạng thái:</strong> 
                                <span class="badge ${product.status ? 'bg-success' : 'bg-danger'}">
                                    ${product.status ? 'Hoạt động' : 'Đã xóa'}
                                </span>
                            </p>
                            <p><strong>Ngày tạo:</strong> ${new Date(product.createdAt).toLocaleString('vi-VN')}</p>
                            <p><strong>Hình ảnh:</strong></p>
                            <div class="text-center">${imageHtml}</div>
                        </div>
                    `,
                    showConfirmButton: false,
                    showCloseButton: true,
                    width: '600px'
                });
            },
            error: function(xhr) {
                showAlert('error', 'Lỗi khi tải thông tin sản phẩm: ' + getErrorMessage(xhr));
            }
        });
    };
    
    // Delete product
    window.deleteProduct = function(productId) {
        Swal.fire({
            title: 'Xác nhận xóa',
            text: 'Bạn có chắc chắn muốn xóa sản phẩm này?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: `/api/products/${productId}`,
                    type: 'DELETE',
                    success: function(response) {
                        showAlert('success', 'Xóa sản phẩm thành công!');
                        productsTable.ajax.reload();
                    },
                    error: function(xhr) {
                        showAlert('error', 'Lỗi khi xóa sản phẩm: ' + getErrorMessage(xhr));
                    }
                });
            }
        });
    };
    
    // Restore product
    window.restoreProduct = function(productId) {
        $.ajax({
            url: `/api/products/${productId}/restore`,
            type: 'PUT',
            success: function(response) {
                showAlert('success', 'Khôi phục sản phẩm thành công!');
                productsTable.ajax.reload();
            },
            error: function(xhr) {
                showAlert('error', 'Lỗi khi khôi phục sản phẩm: ' + getErrorMessage(xhr));
            }
        });
    };
    
    // Permanent delete product
    window.permanentDeleteProduct = function(productId) {
        Swal.fire({
            title: 'Xác nhận xóa vĩnh viễn',
            text: 'Bạn có chắc chắn muốn xóa vĩnh viễn sản phẩm này? Hành động này không thể hoàn tác!',
            icon: 'error',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Xóa vĩnh viễn',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: `/api/products/${productId}/permanent`,
                    type: 'DELETE',
                    success: function(response) {
                        showAlert('success', 'Xóa vĩnh viễn sản phẩm thành công!');
                        productsTable.ajax.reload();
                    },
                    error: function(xhr) {
                        showAlert('error', 'Lỗi khi xóa vĩnh viễn sản phẩm: ' + getErrorMessage(xhr));
                    }
                });
            }
        });
    };
    
    // Display validation errors
    function displayValidationErrors(errors) {
        clearValidationErrors();
        
        if (typeof errors === 'object' && errors !== null) {
            for (const field in errors) {
                const input = $(`[name="${field}"]`);
                if (input.length > 0) {
                    input.addClass('is-invalid');
                    input.siblings('.invalid-feedback').text(errors[field]);
                }
            }
        }
    }
    
    // Clear validation errors
    function clearValidationErrors() {
        $('.form-control').removeClass('is-invalid');
        $('.invalid-feedback').text('');
    }
    
    // Show alert
    function showAlert(type, message) {
        const Toast = Swal.mixin({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true
        });
        
        Toast.fire({
            icon: type,
            title: message
        });
    }
    
    // Get error message from response
    function getErrorMessage(xhr) {
        if (xhr.responseJSON && xhr.responseJSON.message) {
            return xhr.responseJSON.message;
        }
        return 'Lỗi không xác định';
    }
    
    // Debounce function
    function debounce(func, wait, immediate) {
        let timeout;
        return function() {
            const context = this, args = arguments;
            const later = function() {
                timeout = null;
                if (!immediate) func.apply(context, args);
            };
            const callNow = immediate && !timeout;
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
            if (callNow) func.apply(context, args);
        };
    }
});