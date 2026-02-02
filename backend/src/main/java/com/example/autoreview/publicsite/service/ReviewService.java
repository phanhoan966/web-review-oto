package com.example.autoreview.publicsite.service;

import com.example.autoreview.publicsite.dto.response.CommentDto;
import com.example.autoreview.publicsite.dto.request.CreateCommentRequest;
import com.example.autoreview.publicsite.dto.request.CreateReviewRequest;
import com.example.autoreview.publicsite.dto.response.ReviewDto;
import com.example.autoreview.publicsite.dto.response.ReviewListResponse;
import com.example.autoreview.publicsite.dto.request.UpdateReviewRequest;
import com.example.autoreview.domain.Comment;
import com.example.autoreview.domain.CommentLike;
import com.example.autoreview.domain.Review;
import com.example.autoreview.domain.ReviewLike;
import com.example.autoreview.domain.ReviewStatus;
import com.example.autoreview.domain.User;
import com.example.autoreview.domain.VehicleBrand;
import com.example.autoreview.exception.ApiException;
import com.example.autoreview.mapper.DtoMapper;
import com.example.autoreview.repository.CommentLikeRepository;
import com.example.autoreview.repository.CommentRepository;
import com.example.autoreview.repository.ReviewLikeRepository;
import com.example.autoreview.repository.ReviewRepository;
import com.example.autoreview.repository.UserFollowRepository;
import com.example.autoreview.repository.UserRepository;
import com.example.autoreview.repository.VehicleBrandRepository;
import com.example.autoreview.security.Roles;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final VehicleBrandRepository vehicleBrandRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserFollowRepository userFollowRepository;

    public ReviewService(ReviewRepository reviewRepository, VehicleBrandRepository vehicleBrandRepository, UserRepository userRepository, CommentRepository commentRepository, CommentLikeRepository commentLikeRepository, ReviewLikeRepository reviewLikeRepository, UserFollowRepository userFollowRepository) {
        this.reviewRepository = reviewRepository;
        this.vehicleBrandRepository = vehicleBrandRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userFollowRepository = userFollowRepository;
    }

    private void applyAuthorReviewCounts(List<ReviewDto> dtos) {
        Set<Long> authorIds = dtos.stream()
                .map(ReviewDto::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (authorIds.isEmpty()) {
            return;
        }
        Map<Long, Long> counts = reviewRepository.countApprovedByAuthorIds(authorIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        dtos.forEach(dto -> {
            Long authorId = dto.getAuthorId();
            if (authorId != null) {
                dto.setAuthorReviewCount(counts.getOrDefault(authorId, 0L).intValue());
            }
        });
    }

    private void applyCommentAuthorReviewCounts(List<CommentDto> dtos) {
        Set<Long> authorIds = dtos.stream()
                .map(CommentDto::getAuthorUsername)
                .filter(Objects::nonNull)
                .map(username -> userRepository.findByUsername(username).map(User::getId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (authorIds.isEmpty()) {
            return;
        }
        Map<Long, Long> counts = reviewRepository.countApprovedByAuthorIds(authorIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        dtos.forEach(dto -> {
            Long authorId = dto.getAuthorUsername() != null
                    ? userRepository.findByUsername(dto.getAuthorUsername()).map(User::getId).orElse(null)
                    : null;
            if (authorId != null) {
                dto.setAuthorReviewCount(counts.getOrDefault(authorId, 0L).intValue());
            }
        });
    }

    private User findUser(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private void applyReviewLiked(List<ReviewDto> dtos, User user) {
        if (user == null) {
            return;
        }
        Set<Long> ids = dtos.stream()
                .map(ReviewDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Set<Long> likedIds = reviewLikeRepository.findLikedReviewIds(user, ids);
        dtos.forEach(dto -> dto.setLiked(likedIds.contains(dto.getId())));
    }

    private void applyAuthorFollowing(List<ReviewDto> dtos, User user) {
        if (user == null) {
            return;
        }
        Set<Long> authorIds = dtos.stream()
                .map(ReviewDto::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (authorIds.isEmpty()) {
            return;
        }
        Set<Long> followingIds = userFollowRepository.findFollowingIds(user, authorIds);
        dtos.forEach(dto -> dto.setAuthorFollowing(followingIds.contains(dto.getAuthorId())));
    }

    private void applyCommentLiked(List<CommentDto> dtos, User user) {
        if (user == null) {
            return;
        }
        Set<Long> ids = dtos.stream()
                .map(CommentDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Set<Long> likedIds = commentLikeRepository.findLikedCommentIds(user, ids);
        dtos.forEach(dto -> dto.setLiked(likedIds.contains(dto.getId())));
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getFeed(String brand, String fuelType, String priceSegment, int page, int size, String email) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByFilters(ReviewStatus.APPROVED, brand, fuelType, priceSegment, pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        applyAuthorReviewCounts(dtos);
        User user = findUser(email);
        applyReviewLiked(dtos, user);
        applyAuthorFollowing(dtos, user);
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ReviewListResponse search(String query, int page, int size, String email) {
        if (!StringUtils.hasText(query)) {
            return new ReviewListResponse(List.of(), 0);
        }
        PageRequest pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.searchApproved(query.trim(), pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        applyAuthorReviewCounts(dtos);
        User user = findUser(email);
        applyReviewLiked(dtos, user);
        applyAuthorFollowing(dtos, user);
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listByAuthor(String email, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByAuthorEmailOrderByCreatedAtDesc(email, pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        applyAuthorReviewCounts(dtos);
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listPublicByAuthor(Long authorId, int page, int size, String email) {
        User viewer = findUser(email);
        userRepository.findById(authorId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(authorId, ReviewStatus.APPROVED, pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        applyAuthorReviewCounts(dtos);
        applyReviewLiked(dtos, viewer);
        applyAuthorFollowing(dtos, viewer);
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listPublicByAuthorUsername(String username, int page, int size, String email) {
        User viewer = findUser(email);
        userRepository.findByUsername(username).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByAuthorUsernameAndStatusOrderByCreatedAtDesc(username, ReviewStatus.APPROVED, pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        applyAuthorReviewCounts(dtos);
        applyReviewLiked(dtos, viewer);
        applyAuthorFollowing(dtos, viewer);
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findAll(pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        applyAuthorReviewCounts(dtos);
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listByStatus(ReviewStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByStatus(status, pageable);
        List<ReviewDto> dtos = reviews.getContent().stream().map(DtoMapper::toReviewDto).toList();
        applyAuthorReviewCounts(dtos);
        return new ReviewListResponse(dtos, reviews.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> mostViewed(int limit, String email) {
        PageRequest pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "views"));
        List<ReviewDto> dtos = reviewRepository.findMostViewed(pageable).getContent().stream().map(DtoMapper::toReviewDto).toList();
        applyAuthorReviewCounts(dtos);
        User user = findUser(email);
        applyReviewLiked(dtos, user);
        applyAuthorFollowing(dtos, user);
        return dtos;
    }

    @Transactional(readOnly = true)
    public ReviewDto getAdmin(Long id) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        ReviewDto dto = DtoMapper.toReviewDto(review);
        applyAuthorReviewCounts(List.of(dto));
        return dto;
    }

    @Transactional
    public ReviewDto getPublic(Long id, String email) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        User user = findUser(email);
        boolean isOwner = user != null && review.getAuthor() != null && email != null && email.equals(review.getAuthor().getEmail());
        if (review.getStatus() != ReviewStatus.APPROVED && !isOwner) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Review not found");
        }
        if (review.getStatus() == ReviewStatus.APPROVED) {
            review.setViews((review.getViews() == null ? 0 : review.getViews()) + 1);
            reviewRepository.save(review);
        }
        ReviewDto dto = DtoMapper.toReviewDto(review);
        applyAuthorReviewCounts(List.of(dto));
        applyReviewLiked(List.of(dto), user);
        applyAuthorFollowing(List.of(dto), user);
        return dto;
    }

    @Transactional
    public ReviewDto getPublicBySlug(String slug, String email) {
        Review review = reviewRepository.findBySlug(slug).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        User user = findUser(email);
        boolean isOwner = user != null && review.getAuthor() != null && email != null && email.equals(review.getAuthor().getEmail());
        if (review.getStatus() != ReviewStatus.APPROVED && !isOwner) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Review not found");
        }
        if (review.getStatus() == ReviewStatus.APPROVED) {
            review.setViews((review.getViews() == null ? 0 : review.getViews()) + 1);
            reviewRepository.save(review);
        }
        ReviewDto dto = DtoMapper.toReviewDto(review);
        applyAuthorReviewCounts(List.of(dto));
        applyReviewLiked(List.of(dto), user);
        applyAuthorFollowing(List.of(dto), user);
        return dto;
    }

    @Transactional
    public ReviewDto create(String authorEmail, CreateReviewRequest request) {
        User author = userRepository.findByEmail(authorEmail).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        VehicleBrand brand = vehicleBrandRepository.findById(request.getBrandId()).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Brand not found"));
        Review review = new Review();
        review.setTitle(request.getTitle());
        review.setSlug(request.getSlug());
        review.setExcerpt(request.getExcerpt());
        review.setContent(request.getContent());
        review.setHeroImageUrl(sanitizeHeroImageUrl(request.getHeroImageUrl()));
        review.setVehicleModel(request.getVehicleModel());
        review.setVehicleYear(request.getVehicleYear());
        review.setFuelType(request.getFuelType());
        review.setPriceSegment(request.getPriceSegment());
        review.setBrand(brand);
        review.setAuthor(author);
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());
        review.setLikes(0);
        review.setCommentsCount(0);
        review.setViews(0);
        reviewRepository.save(review);
        ReviewDto dto = DtoMapper.toReviewDto(review);
        applyAuthorReviewCounts(List.of(dto));
        return dto;
    }

    @Transactional
    public ReviewDto updateOwn(Long id, String email, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getAuthor().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not owner");
        }
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending review can be edited");
        }
        review.setTitle(request.getTitle());
        review.setSlug(request.getSlug());
        review.setExcerpt(request.getExcerpt());
        review.setContent(request.getContent());
        review.setHeroImageUrl(sanitizeHeroImageUrl(request.getHeroImageUrl()));
        review.setVehicleModel(request.getVehicleModel());
        review.setVehicleYear(request.getVehicleYear());
        review.setFuelType(request.getFuelType());
        review.setPriceSegment(request.getPriceSegment());
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);
        ReviewDto dto = DtoMapper.toReviewDto(review);
        applyAuthorReviewCounts(List.of(dto));
        return dto;
    }

    @Transactional
    public ReviewDto updateOwnBySlug(String slug, String email, UpdateReviewRequest request) {
        Review review = reviewRepository.findBySlug(slug).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getAuthor().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not owner");
        }
        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending review can be edited");
        }
        review.setTitle(request.getTitle());
        review.setSlug(request.getSlug());
        review.setExcerpt(request.getExcerpt());
        review.setContent(request.getContent());
        review.setHeroImageUrl(sanitizeHeroImageUrl(request.getHeroImageUrl()));
        review.setVehicleModel(request.getVehicleModel());
        review.setVehicleYear(request.getVehicleYear());
        review.setFuelType(request.getFuelType());
        review.setPriceSegment(request.getPriceSegment());
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);
        ReviewDto dto = DtoMapper.toReviewDto(review);
        applyAuthorReviewCounts(List.of(dto));
        return dto;
    }

    @Transactional
    public void approve(Long id, String approverEmail, boolean approve) {
        setStatus(id, approverEmail, approve ? ReviewStatus.APPROVED : ReviewStatus.REJECTED);
    }

    @Transactional
    public void hideOwn(Long id, String email) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getAuthor().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not owner");
        }
        if (review.getStatus() == ReviewStatus.HIDDEN) {
            return;
        }
        review.setStatus(ReviewStatus.HIDDEN);
        review.setPublishedAt(null);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);
    }

    @Transactional
    public void unhideOwn(Long id, String email) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getAuthor().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not owner");
        }
        if (review.getStatus() != ReviewStatus.HIDDEN) {
            return;
        }
        review.setStatus(ReviewStatus.APPROVED);
        review.setUpdatedAt(Instant.now());
        review.setPublishedAt(Instant.now());
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteOwn(Long id, String email) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getAuthor().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not owner");
        }
        commentRepository.deleteByReview(review);
        reviewRepository.delete(review);
    }

    @Transactional
    public void setStatus(Long id, String approverEmail, ReviewStatus status) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        User approver = userRepository.findByEmail(approverEmail).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (!hasAdminRole(approver)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        review.setStatus(status);
        review.setPublishedAt(status == ReviewStatus.APPROVED ? Instant.now() : null);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);
    }

    @Transactional
    public void restoreRejected(Long id, String approverEmail) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        User approver = userRepository.findByEmail(approverEmail).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (!hasAdminRole(approver)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        if (review.getStatus() != ReviewStatus.REJECTED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ khôi phục được bài bị từ chối");
        }
        review.setStatus(ReviewStatus.PENDING);
        review.setPublishedAt(null);
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> listPending(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByStatus(ReviewStatus.PENDING, pageable).map(DtoMapper::toReviewDto);
    }

    @Transactional
    public CommentDto addComment(Long reviewId, String authorEmail, CreateCommentRequest request) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (review.getStatus() != ReviewStatus.APPROVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot comment on unapproved review");
        }
        User author = null;
        if (authorEmail != null) {
            author = userRepository.findByEmail(authorEmail).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        }
        try {
            Comment comment = new Comment();
            comment.setContent(request.getContent());
            comment.setAuthor(author);
            comment.setReview(review);
            comment.setAnonymous(request.isAnonymous());
            comment.setCreatedAt(Instant.now());
            if (request.getParentId() != null) {
                Comment parent = commentRepository.findById(request.getParentId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Parent comment not found"));
                if (!parent.getReview().getId().equals(reviewId)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Parent comment không thuộc bài viết này");
                }
                comment.setParent(parent);
            }
            Comment saved = commentRepository.save(comment);
            review.setCommentsCount((review.getCommentsCount() == null ? 0 : review.getCommentsCount()) + 1);
            reviewRepository.save(review);
            CommentDto dto = DtoMapper.toCommentDto(saved);
            applyCommentAuthorReviewCounts(List.of(dto));
            return dto;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Không thể lưu bình luận");
        }
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listComments(Long reviewId, int page, int size, String sort, String email) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        User user = findUser(email);
        boolean isOwner = user != null && review.getAuthor() != null && email != null && email.equals(review.getAuthor().getEmail());
        if (review.getStatus() != ReviewStatus.APPROVED && !isOwner) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Comments unavailable for unapproved review");
        }
        PageRequest pageable = PageRequest.of(page, size);
        if (!"latest".equalsIgnoreCase(sort) && !"top".equalsIgnoreCase(sort)) {
            sort = "top";
        }
        List<Comment> all = commentRepository.findByReviewOrderByCreatedAtAsc(review);
        java.util.Map<Long, Comment> byId = all.stream()
                .collect(java.util.stream.Collectors.toMap(Comment::getId, c -> c));
        java.util.function.Function<Comment, Comment> rootOf = c -> {
            Comment current = c;
            java.util.Set<Long> seen = new java.util.HashSet<>();
            while (current.getParent() != null && !seen.contains(current.getParent().getId())) {
                seen.add(current.getParent().getId());
                current = byId.getOrDefault(current.getParent().getId(), current.getParent());
                if (current == null) {
                    break;
                }
            }
            return current == null ? c : current;
        };
        java.util.List<Comment> roots = all.stream().filter(c -> c.getParent() == null).toList();
        java.util.Comparator<Comment> rootComparator = "latest".equalsIgnoreCase(sort)
                ? java.util.Comparator.comparing(Comment::getCreatedAt, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())).reversed()
                : java.util.Comparator.comparing(Comment::getLikes, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())).reversed()
                        .thenComparing(java.util.Comparator.comparing(Comment::getCreatedAt, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())).reversed());
        roots = roots.stream().sorted(rootComparator).toList();
        int from = Math.toIntExact((long) page * size);
        if (from >= roots.size()) {
            return java.util.List.of();
        }
        int to = Math.min(roots.size(), from + size);
        java.util.List<Comment> selectedRoots = roots.subList(from, to);
        java.util.Set<Long> rootIds = selectedRoots.stream().map(Comment::getId).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, java.util.List<Comment>> byRoot = new java.util.HashMap<>();
        all.forEach(c -> {
            Comment root = rootOf.apply(c);
            if (root != null && rootIds.contains(root.getId())) {
                byRoot.computeIfAbsent(root.getId(), k -> new java.util.ArrayList<>()).add(c);
            }
        });
        java.util.List<CommentDto> result = new java.util.ArrayList<>();
        for (Comment root : selectedRoots) {
            java.util.List<Comment> branch = byRoot.getOrDefault(root.getId(), java.util.List.of());
            branch = branch.stream()
                    .sorted(java.util.Comparator.comparing(Comment::getCreatedAt, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())))
                    .toList();
            java.util.List<Comment> ordered = new java.util.ArrayList<>();
            ordered.add(root);
            branch.stream().filter(c -> !c.getId().equals(root.getId())).forEach(ordered::add);
            result.addAll(ordered.stream().map(DtoMapper::toCommentDto).toList());
        }
        applyCommentAuthorReviewCounts(result);
        applyCommentLiked(result, findUser(email));
        return result;
    }

    @Transactional
    public void likeComment(Long commentId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
            return;
        }
        CommentLike like = new CommentLike();
        like.setComment(comment);
        like.setUser(user);
        like.setCreatedAt(Instant.now());
        commentLikeRepository.save(like);
        comment.setLikes((comment.getLikes() == null ? 0 : comment.getLikes()) + 1);
        commentRepository.save(comment);
    }

    @Transactional
    public void unlikeComment(Long commentId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        CommentLike like = commentLikeRepository.findByCommentAndUser(comment, user).orElse(null);
        if (like == null) {
            return;
        }
        commentLikeRepository.delete(like);
        int current = comment.getLikes() == null ? 0 : comment.getLikes();
        comment.setLikes(Math.max(0, current - 1));
        commentRepository.save(comment);
    }

    @Transactional
    public void likeReview(Long reviewId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập"));
        Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.APPROVED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (reviewLikeRepository.existsByReviewAndUser(review, user)) {
            return;
        }
        ReviewLike like = new ReviewLike();
        like.setReview(review);
        like.setUser(user);
        like.setCreatedAt(Instant.now());
        reviewLikeRepository.save(like);
        review.setLikes((review.getLikes() == null ? 0 : review.getLikes()) + 1);
        reviewRepository.save(review);
    }

    @Transactional
    public void unlikeReview(Long reviewId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập"));
        Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.APPROVED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        ReviewLike like = reviewLikeRepository.findByReviewAndUser(review, user).orElse(null);
        if (like == null) {
            return;
        }
        reviewLikeRepository.delete(like);
        int current = review.getLikes() == null ? 0 : review.getLikes();
        review.setLikes(Math.max(0, current - 1));
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        boolean canDelete = hasAdminRole(user) || (comment.getAuthor() != null && comment.getAuthor().getEmail().equals(email));
        if (!canDelete) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        Review review = comment.getReview();
        commentRepository.delete(comment);
        if (review.getCommentsCount() != null && review.getCommentsCount() > 0) {
            review.setCommentsCount(review.getCommentsCount() - 1);
            reviewRepository.save(review);
        }
    }

    private String sanitizeHeroImageUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String url = value.trim();
        if (url.startsWith("data:")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận URL ảnh");
        }
        if (url.length() > 512) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "URL ảnh quá dài");
        }
        if (url.startsWith("/uploads/") || url.startsWith("uploads/")) {
            return url.startsWith("/") ? url : "/" + url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "URL ảnh không hợp lệ");
    }

    private boolean hasAdminRole(User user) {
        return user.getRoles().contains(Roles.ADMIN) || user.getRoles().contains(Roles.MANAGER) || user.getRoles().contains(Roles.SYSTEM_ADMIN);
    }
}
