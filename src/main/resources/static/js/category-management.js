// Category Management JavaScript
$(document).ready(function() {
    let categoriesTable;
    let isEditMode = false;
    let currentCategoryId = null;

    // Initialize DataTable
    initializeDataTable();
    
    // Event handlers
    $('#btnAddCategory').on('click', showAddCategoryModal);
    $('#categoryForm').on('submit', handleCategoryFormSubmit);
    $('#statusFilter').on('change', filterCategories);
    $('#searchKeyword').on('input', debounce(filterCategories, 300));
    
    // Initialize DataTable
    function initializeDataTable() {
        categoriesTable = $('#categoriesTable').DataTable({
            processing: true,
            serverSide: false,
            ajax: {
                url: '/api/categories',
                type: 'GET',
                data: function(d) {
                    return {
                        status: $('#statusFilter').val(),
                        keyword: $('#searchKeyword').val() || null
                    };
                },
                dataSrc: function(json) {
                    return json;
                }
            },
            columns: [
                { data: 'categoryId', title: 'ID' },
                { 
                    data: 'icon', 
                    title: 'Icon',
                    render: function(data, type, row) {
                        if (data && data.trim() !== '') {
                            return `<i class="${data}" style="font-size: 24px;"></i>`;
                        }
                        return '<span class="text-muted">Không có</span>';
                    },
                    orderable: false
                },
                { data: 'categoryName', title: 'Tên danh mục' },
                { 
                    data: 'description', 
                    title: 'Mô tả',
                    render: function(data, type, row) {
                        if (data && data.length > 50) {
                            return data.substring(0, 50) + '...';
                        }
                        return data || '<span class="text-muted">Không có</span>';
                    }
                },
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
                                <button class="btn btn-sm btn-info me-1" onclick="viewCategory(${row.categoryId})" title="Xem">
                                    <i class="fas fa-eye"></i>
                                </button>
                                <button class="btn btn-sm btn-warning me-1" onclick="editCategory(${row.categoryId})" title="Sửa">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-danger" onclick="deleteCategory(${row.categoryId})" title="Xóa">
                                    <i class="fas fa-trash"></i>
                                </button>
                            `;
                        } else {
                            buttons += `
                                <button class="btn btn-sm btn-success me-1" onclick="restoreCategory(${row.categoryId})" title="Khôi phục">
                                    <i class="fas fa-undo"></i>
                                </button>
                                <button class="btn btn-sm btn-danger" onclick="permanentDeleteCategory(${row.categoryId})" title="Xóa vĩnh viễn">
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
    
    // Filter categories
    function filterCategories() {
        categoriesTable.ajax.reload();
    }
    
    // Show add category modal
    function showAddCategoryModal() {
        isEditMode = false;
        currentCategoryId = null;
        $('#categoryModalLabel').text('Thêm danh mục');
        $('#categoryForm')[0].reset();
        $('#status').prop('checked', true);
        clearValidationErrors();
        $('#categoryModal').modal('show');
    }
    
    // Handle category form submit
    function handleCategoryFormSubmit(e) {
        e.preventDefault();
        
        const formData = {
            categoryName: $('#categoryName').val(),
            description: $('#description').val(),
            icon: $('#icon').val(),
            status: $('#status').is(':checked')
        };
        
        if (isEditMode) {
            formData.categoryId = currentCategoryId;
            updateCategory(formData);
        } else {
            createCategory(formData);
        }
    }
    
    // Create category
    function createCategory(formData) {
        $.ajax({
            url: '/api/categories',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function(response) {
                $('#categoryModal').modal('hide');
                showAlert('success', 'Thêm danh mục thành công!');
                categoriesTable.ajax.reload();
            },
            error: function(xhr) {
                if (xhr.status === 400) {
                    displayValidationErrors(xhr.responseJSON);
                } else {
                    showAlert('error', 'Lỗi khi thêm danh mục: ' + getErrorMessage(xhr));
                }
            }
        });
    }
    
    // Update category
    function updateCategory(formData) {
        $.ajax({
            url: `/api/categories/${currentCategoryId}`,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function(response) {
                $('#categoryModal').modal('hide');
                showAlert('success', 'Cập nhật danh mục thành công!');
                categoriesTable.ajax.reload();
            },
            error: function(xhr) {
                if (xhr.status === 400 || xhr.status === 422) {
                    displayValidationErrors(xhr.responseJSON);
                } else {
                    showAlert('error', 'Lỗi khi cập nhật danh mục: ' + getErrorMessage(xhr));
                }
            }
        });
    }
    
    // Edit category
    window.editCategory = function(categoryId) {
        $.ajax({
            url: `/api/categories/${categoryId}`,
            type: 'GET',
            success: function(category) {
                isEditMode = true;
                currentCategoryId = categoryId;
                $('#categoryModalLabel').text('Sửa danh mục');
                
                $('#categoryName').val(category.categoryName);
                $('#description').val(category.description || '');
                $('#icon').val(category.icon || '');
                $('#status').prop('checked', category.status);
                
                clearValidationErrors();
                $('#categoryModal').modal('show');
            },
            error: function(xhr) {
                showAlert('error', 'Lỗi khi tải thông tin danh mục: ' + getErrorMessage(xhr));
            }
        });
    };
    
    // View category
    window.viewCategory = function(categoryId) {
        $.ajax({
            url: `/api/categories/${categoryId}`,
            type: 'GET',
            success: function(category) {
                let iconHtml = category.icon ? 
                    `<i class="${category.icon}" style="font-size: 48px;"></i>` :
                    '<span class="text-muted">Không có icon</span>';
                
                Swal.fire({
                    title: category.categoryName,
                    html: `
                        <div class="text-start">
                            <p><strong>Mô tả:</strong> ${category.description || 'Không có'}</p>
                            <p><strong>Trạng thái:</strong> 
                                <span class="badge ${category.status ? 'bg-success' : 'bg-danger'}">
                                    ${category.status ? 'Hoạt động' : 'Đã xóa'}
                                </span>
                            </p>
                            <p><strong>Số sản phẩm:</strong> ${category.productCount || 0}</p>
                            <p><strong>Ngày tạo:</strong> ${new Date(category.createdAt).toLocaleString('vi-VN')}</p>
                            <p><strong>Icon:</strong></p>
                            <div class="text-center">${iconHtml}</div>
                        </div>
                    `,
                    showConfirmButton: false,
                    showCloseButton: true,
                    width: '500px'
                });
            },
            error: function(xhr) {
                showAlert('error', 'Lỗi khi tải thông tin danh mục: ' + getErrorMessage(xhr));
            }
        });
    };
    
    // Delete category
    window.deleteCategory = function(categoryId) {
        Swal.fire({
            title: 'Xác nhận xóa',
            text: 'Bạn có chắc chắn muốn xóa danh mục này?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: `/api/categories/${categoryId}`,
                    type: 'DELETE',
                    success: function(response) {
                        showAlert('success', 'Xóa danh mục thành công!');
                        categoriesTable.ajax.reload();
                    },
                    error: function(xhr) {
                        showAlert('error', 'Lỗi khi xóa danh mục: ' + getErrorMessage(xhr));
                    }
                });
            }
        });
    };
    
    // Restore category
    window.restoreCategory = function(categoryId) {
        $.ajax({
            url: `/api/categories/${categoryId}/restore`,
            type: 'PUT',
            success: function(response) {
                showAlert('success', 'Khôi phục danh mục thành công!');
                categoriesTable.ajax.reload();
            },
            error: function(xhr) {
                showAlert('error', 'Lỗi khi khôi phục danh mục: ' + getErrorMessage(xhr));
            }
        });
    };
    
    // Permanent delete category
    window.permanentDeleteCategory = function(categoryId) {
        Swal.fire({
            title: 'Xác nhận xóa vĩnh viễn',
            text: 'Bạn có chắc chắn muốn xóa vĩnh viễn danh mục này? Hành động này không thể hoàn tác!',
            icon: 'error',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Xóa vĩnh viễn',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: `/api/categories/${categoryId}/permanent`,
                    type: 'DELETE',
                    success: function(response) {
                        showAlert('success', 'Xóa vĩnh viễn danh mục thành công!');
                        categoriesTable.ajax.reload();
                    },
                    error: function(xhr) {
                        showAlert('error', 'Lỗi khi xóa vĩnh viễn danh mục: ' + getErrorMessage(xhr));
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

// Make functions available globally for category management
window.showAddCategoryModal = function() {
    $('#btnAddCategory').trigger('click');
};