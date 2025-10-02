package com.example.webapp.service;

import com.example.webapp.model.CartItem;
import com.example.webapp.model.Product;
import com.example.webapp.model.User;
import com.example.webapp.repository.CartItemRepository;
import com.example.webapp.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CartService {

    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;

    public CartService(CartItemRepository cartItemRepo, ProductRepository productRepo) {
        this.cartItemRepo = cartItemRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public void addToCart(HttpSession session, Long productId, int quantity) {
        Product product = productRepo.findById(productId).orElse(null);
        if (product == null) {
            return;  // Or throw exception
        }

        int availableStock = product.getQuantity();  // Use stock field
        if (quantity > availableStock) {
            // Validation: Limit to available stock
            quantity = availableStock;
            // Could throw exception or log: System.err.println("Requested quantity exceeds stock; limited to " + quantity);
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            // Logged in: add to DB
            Optional<CartItem> optItem = cartItemRepo.findByUserAndProduct(currentUser, product);
            CartItem item;
            if (optItem.isPresent()) {
                item = optItem.get();
                int newQty = item.getQuantity() + quantity;
                if (newQty > availableStock) {
                    newQty = availableStock;  // Validate total in cart too
                }
                item.setQuantity(newQty);
            } else {
                item = new CartItem();
                item.setUser(currentUser);
                item.setProduct(product);
                item.setQuantity(quantity);
            }
            cartItemRepo.save(item);
        } else {
            // Guest: add to session
            @SuppressWarnings("unchecked")
            Map<Long, Integer> guestCart = (Map<Long, Integer>) session.getAttribute("guestCart");
            if (guestCart == null) {
                guestCart = new HashMap<>();
            }
            int currentGuestQty = guestCart.getOrDefault(productId, 0);
            int newGuestQty = currentGuestQty + quantity;
            if (newGuestQty > availableStock) {
                newGuestQty = availableStock;  // Validate for guest too
            }
            guestCart.put(productId, newGuestQty);
            session.setAttribute("guestCart", guestCart);
        }
    }

    public List<CartItem> getCartItems(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        List<CartItem> cartItems = new ArrayList<>();

        if (currentUser != null) {
            // Logged in
            cartItems = cartItemRepo.findByUser(currentUser);
        } else {
            // Guest
            @SuppressWarnings("unchecked")
            Map<Long, Integer> guestCart = (Map<Long, Integer>) session.getAttribute("guestCart");
            if (guestCart != null) {
                for (Map.Entry<Long, Integer> entry : guestCart.entrySet()) {
                    Product p = productRepo.findById(entry.getKey()).orElse(null);
                    if (p != null) {
                        CartItem tempItem = new CartItem();
                        tempItem.setProduct(p);
                        tempItem.setQuantity(entry.getValue());
                        cartItems.add(tempItem);
                    }
                }
            }
        }
        return cartItems;
    }

    public double calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    @Transactional
    public void removeFromCart(HttpSession session, Long productId, int quantity) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            // Logged in
            Optional<CartItem> optItem = cartItemRepo.findByUserAndProductId(currentUser, productId);
            if (optItem.isPresent()) {
                CartItem item = optItem.get();
                item.setQuantity(item.getQuantity() - quantity);
                if (item.getQuantity() <= 0) {
                    cartItemRepo.delete(item);
                } else {
                    cartItemRepo.save(item);
                }
            }
        } else {
            // Guest
            @SuppressWarnings("unchecked")
            Map<Long, Integer> guestCart = (Map<Long, Integer>) session.getAttribute("guestCart");
            if (guestCart != null) {
                guestCart.computeIfPresent(productId, (k, v) -> v > quantity ? v - quantity : null);
                if (guestCart.isEmpty()) {
                    session.removeAttribute("guestCart");
                } else {
                    session.setAttribute("guestCart", guestCart);
                }
            }
        }
    }

    @Transactional
    public void mergeGuestCart(HttpSession session, User user) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> guestCart = (Map<Long, Integer>) session.getAttribute("guestCart");
        if (guestCart != null && !guestCart.isEmpty()) {
            for (Map.Entry<Long, Integer> entry : guestCart.entrySet()) {
                Long prodId = entry.getKey();
                int qty = entry.getValue();
                Product prod = productRepo.findById(prodId).orElse(null);
                if (prod != null) {
                    int availableStock = prod.getQuantity();
                    if (qty > availableStock) {
                        qty = availableStock;  // Validate during merge too
                    }
                    Optional<CartItem> optCi = cartItemRepo.findByUserAndProduct(user, prod);
                    CartItem ci;
                    if (optCi.isPresent()) {
                        ci = optCi.get();
                        int newQty = ci.getQuantity() + qty;
                        if (newQty > availableStock) {
                            newQty = availableStock;
                        }
                        ci.setQuantity(newQty);
                    } else {
                        ci = new CartItem();
                        ci.setUser(user);
                        ci.setProduct(prod);
                        ci.setQuantity(qty);
                    }
                    cartItemRepo.save(ci);
                }
            }
            session.removeAttribute("guestCart");
        }
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepo.deleteByUser(user);
    }

    // Helper to get available quantity for a product
    public int getAvailableQuantity(Long productId) {
        Product product = productRepo.findById(productId).orElse(null);
        return product != null ? product.getQuantity() : 0;
    }
}