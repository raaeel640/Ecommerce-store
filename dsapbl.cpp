
#include <iostream>
#include <string>
#include <cstring>




using namespace std;

/* ------------------ PRODUCT STRUCT ------------------ */
struct Product {
    int id;
    char name[50];
    int price;
};

/* ------------------ AVL TREE ------------------ */
struct AVLNode {
    Product p;
    int height;
    AVLNode *left, *right;
};

int height(AVLNode* n) {
    return n ? n->height : 0;
}

AVLNode* newNode(Product p) {
    AVLNode* n = new AVLNode;
    n->p = p;
    n->left = n->right = nullptr;
    n->height = 1;
    return n;
}

int getBalance(AVLNode* n) {
    return n ? height(n->left) - height(n->right) : 0;
}

AVLNode* rightRotate(AVLNode* y) {
    AVLNode* x = y->left;
    y->left = x->right;
    x->right = y;

    y->height = 1 + max(height(y->left), height(y->right));
    x->height = 1 + max(height(x->left), height(x->right));

    return x;
}

AVLNode* leftRotate(AVLNode* x) {
    AVLNode* y = x->right;
    x->right = y->left;
    y->left = x;

    x->height = 1 + max(height(x->left), height(x->right));
    y->height = 1 + max(height(y->left), height(y->right));

    return y;
}

AVLNode* insertAVL(AVLNode* node, Product p) {
    if (!node) return newNode(p);

    if (p.id < node->p.id) node->left = insertAVL(node->left, p);
    else if (p.id > node->p.id) node->right = insertAVL(node->right, p);
    else {
        cout << "Product ID exists!\n";
        return node;
    }

    node->height = 1 + max(height(node->left), height(node->right));
    int bal = getBalance(node);

    if (bal > 1 && p.id < node->left->p.id) return rightRotate(node);
    if (bal < -1 && p.id > node->right->p.id) return leftRotate(node);
    if (bal > 1 && p.id > node->left->p.id) {
        node->left = leftRotate(node->left);
        return rightRotate(node);
    }
    if (bal < -1 && p.id < node->right->p.id) {
        node->right = rightRotate(node->right);
        return leftRotate(node);
    }

    return node;
}

void inOrder(AVLNode* root) {
    if (!root) return;
    inOrder(root->left);
    cout << root->p.id << " - " << root->p.name << " - Rs." << root->p.price << endl;
    inOrder(root->right);
}

Product* searchAVL(AVLNode* root, int id) {
    if (!root) return nullptr;
    if (root->p.id == id) return &root->p;
    if (id < root->p.id) return searchAVL(root->left, id);
    return searchAVL(root->right, id);
}

Product* searchByName(AVLNode* root, const char* name) {
    if (!root) return nullptr;
    int cmp = strcmp(name, root->p.name);
    if (cmp == 0) return &root->p;
    Product* left = searchByName(root->left, name);
    if (left) return left;
    return searchByName(root->right, name);
}

/* ------------------ FILE HANDLING PRODUCTS ------------------ */
void saveProductsToFile(AVLNode* root, FILE* fp) {
    if (!root) return;
    saveProductsToFile(root->left, fp);
    fprintf(fp, "%d %s %d\n", root->p.id, root->p.name, root->p.price);
    saveProductsToFile(root->right, fp);
}

void saveAllProducts(AVLNode* root) {
    FILE* fp = fopen("products.txt", "w");
    if (!fp) return;
    saveProductsToFile(root, fp);
    fclose(fp);
}

void loadProducts(AVLNode*& root) {
    FILE* fp = fopen("products.txt", "r");
    if (!fp) return;
    Product p;
    while (fscanf(fp, "%d %s %d", &p.id, p.name, &p.price) != EOF) {
        root = insertAVL(root, p);
    }
    fclose(fp);
}

/* ------------------ USERS ------------------ */
struct User {
    char username[30];
    char password[30];
};

User userTable[100];
int userCount = 0;

void saveUsers() {
    FILE* fp = fopen("users.txt", "w");
    if (!fp) return;
    for (int i = 0; i < userCount; i++)
        fprintf(fp, "%s %s\n", userTable[i].username, userTable[i].password);
    fclose(fp);
}

void loadUsers() {
    FILE* fp = fopen("users.txt", "r");
    if (!fp) return;
    User u;
    while (fscanf(fp, "%s %s", u.username, u.password) != EOF) {
        userTable[userCount++] = u;
    }
    fclose(fp);
}

void addUser() {
    char u[30], p[30];
    cout << "Enter new username: "; cin >> u;
    cout << "Enter password: "; cin >> p;

    strcpy(userTable[userCount].username, u);
    strcpy(userTable[userCount].password, p);
    userCount++;
    saveUsers();

    cout << "User registered successfully!\n";
}

bool loginUser(char* currentUser) {
    char u[30], p[30];
    cout << "Username: "; cin >> u;
    cout << "Password: "; cin >> p;

    for (int i = 0; i < userCount; i++) {
        if (strcmp(userTable[i].username, u) == 0 && strcmp(userTable[i].password, p) == 0) {
            strcpy(currentUser, u);
            return true;
        }
    }
    return false;
}

/* ------------------ CART ------------------ */
struct CartNode {
    Product p;
    CartNode* next;
};

CartNode* cartHead = nullptr;
char currentUser[30]; 
void saveCart() {
    if (currentUser[0] == '\0') return;
    char filename[50];
    sprintf(filename, "cart_%s.txt", currentUser);
    FILE* fp = fopen(filename, "w");
    if (!fp) return;
    CartNode* t = cartHead;
    while (t) {
        fprintf(fp, "%s %d\n", t->p.name, t->p.price);
        t = t->next;
    }
    fclose(fp);
}

void loadCart() {
    cartHead = nullptr; 
    if (currentUser[0] == '\0') return;
    char filename[50];
    sprintf(filename, "cart_%s.txt", currentUser);
    FILE* fp = fopen(filename, "r");
    if (!fp) return;
    Product p;
    while (fscanf(fp, "%s %d", p.name, &p.price) != EOF) {
        CartNode* n = new CartNode;
        n->p = p;
        n->next = cartHead;
        cartHead = n;
    }
    fclose(fp);
}

void addToCart(Product p) {
    CartNode* n = new CartNode;
    n->p = p;
    n->next = cartHead;
    cartHead = n;
    saveCart();
    cout << p.name << " added to cart.\n";
}

void viewCart() {
    if (!cartHead) {
        cout << "Cart is empty.\n";
        return;
    }
    CartNode* t = cartHead;
    int total = 0;
    cout << "\n--- YOUR CART ---\n";
    while (t) {
        cout << t->p.name << " - Rs." << t->p.price << endl;
        total += t->p.price;
        t = t->next;
    }
    cout << "Total Amount: Rs." << total << endl;
}

/* ------------------ ORDERS (Priority Queue) ------------------ */
struct Order {
    int priority;
    Product p;
};

Order orderHeap[50];
int orderSize = 0;

void saveOrders() {
    FILE* fp = fopen("orders.txt", "w");
    if (!fp) return;
    for (int i = 1; i <= orderSize; i++)
        fprintf(fp, "%s %d %d\n", orderHeap[i].p.name, orderHeap[i].p.price, orderHeap[i].priority);
    fclose(fp);
}

void loadOrders() {
    FILE* fp = fopen("orders.txt", "r");
    if (!fp) return;
    Order o;
    while (fscanf(fp, "%s %d %d", o.p.name, &o.p.price, &o.priority) != EOF)
        orderHeap[++orderSize] = o;
    fclose(fp);
}

void heapifyDown(int i) {
    int largest = i, left = i * 2, right = i * 2 + 1;
    if (left <= orderSize && orderHeap[left].priority > orderHeap[largest].priority) largest = left;
    if (right <= orderSize && orderHeap[right].priority > orderHeap[largest].priority) largest = right;
    if (largest != i) { swap(orderHeap[i], orderHeap[largest]); heapifyDown(largest); }
}

void pushOrder(Order o) {
    orderHeap[++orderSize] = o;
    int i = orderSize;
    while (i > 1 && orderHeap[i/2].priority < orderHeap[i].priority) { swap(orderHeap[i], orderHeap[i/2]); i /= 2; }
    saveOrders();
    cout << "Order added successfully.\n";
}

void processOrder() {
    if (orderSize == 0) { cout << "No orders to process.\n"; return; }
    cout << "Processed Order: " << orderHeap[1].p.name << endl;
    orderHeap[1] = orderHeap[orderSize--];
    heapifyDown(1);
    saveOrders();
}

/* ------------------ USER-PRODUCT RELATION ------------------ */
int graph[100][100] = {0};

void saveRelations() {
    FILE* fp = fopen("relations.txt", "w");
    if (!fp) return;
    for (int i = 0; i < 100; i++)
        for (int j = 0; j < 100; j++)
            if (graph[i][j]) fprintf(fp, "%d %d\n", i, j);
    fclose(fp);
}

void loadRelations() {
    FILE* fp = fopen("relations.txt", "r");
    if (!fp) return;
    int u, p;
    while (fscanf(fp, "%d %d", &u, &p) != EOF) graph[u][p] = 1;
    fclose(fp);
}

void addRelation(int uid, int pid) {
    graph[uid][pid] = 1;
    saveRelations();
    cout << "Relation added (User selected a product).\n";
}


int main() {
    AVLNode* root = nullptr;
    loadProducts(root);
    loadUsers();
    loadOrders();
    loadRelations();

    cout << "--------------------------\n  REGISTER ADMIN FIRST    \n--------------------------\n";
    addUser();

    cout << "\n--------- LOGIN ---------\n";
    if (!loginUser(currentUser)) { cout << "Invalid login! Exiting.\n"; return 0; }
    cout << "\nLogin Successful!\n";

    loadCart(); 

    int choice;
    do {
        cout << "\n====== E-COMMERCE MENU ======\n";
        cout << "1. Add Product (AVL)\n2. View All Products\n3. Search Product\n4. Add to Cart\n5. View Cart\n6. Place Order (Priority)\n7. Process Order\n8. Register New User\n9. Add User–Product Relation\n10. Exit\nEnter choice: ";
        cin >> choice;

        switch (choice) {
case 1: {
    Product p;
    cout << "Enter Product ID: "; cin >> p.id;
    cout << "Enter Name: "; cin >> p.name;
    cout << "Enter Price: "; cin >> p.price;

    if (searchAVL(root, p.id)) {
        cout << "Product ID exists!\n";
    } else {
        root = insertAVL(root, p);
        cout << "Product added.\n";
        saveAllProducts(root);
    }
    break;
}

            
            case 2: inOrder(root); break;
            case 3: {
                int id; cout << "Enter ID: "; cin >> id;
                Product* r = searchAVL(root, id);
                cout << (r ? r->name + string(" found.") : "Not found.") << endl;
                break;
            }
            case 4: {
                char pname[50];
                cout << "Enter Product Name: "; cin >> pname;
                Product* r = searchByName(root, pname);
                if (r) addToCart(*r);
                else cout << "Product not found.\n";
                break;
            }
            case 5: viewCart(); break;
            case 6: {
                int id; cout << "Enter ID: "; cin >> id;
                Product* r = searchAVL(root, id);
                if (r) { Order o; o.priority = rand() % 10; o.p = *r; pushOrder(o); }
                break;
            }
            case 7: processOrder(); break;
            case 8: addUser(); break;
            case 9: {
                int uid, pid;
                cout << "Enter User Index (0-" << userCount-1 << "): "; cin >> uid;
                cout << "Enter Product ID: "; cin >> pid;
                addRelation(uid, pid);
                break;
            }
        }
    } while (choice != 10);

    cout << "Thank you for using the system!\n";
    return 0;
}
