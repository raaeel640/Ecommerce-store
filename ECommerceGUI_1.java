import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class Product {
    int id;
    String name;
    int price;
    Product(int id,String name,int price){this.id=id;this.name=name;this.price=price;}
    @Override public String toString(){return name+" - Rs."+price+" (ID:"+id+")";}
}

class AVLNode {
    Product p; int height; AVLNode left,right;
    AVLNode(Product p){this.p=p;height=1;}
}

class AVLTree {
    AVLNode root;
    int height(AVLNode n){return n==null?0:n.height;}
    int getBalance(AVLNode n){return n==null?0:height(n.left)-height(n.right);}
    AVLNode rightRotate(AVLNode y){
        AVLNode x=y.left;AVLNode T2=x.right;
        x.right=y;y.left=T2;
        y.height=Math.max(height(y.left),height(y.right))+1;
        x.height=Math.max(height(x.left),height(x.right))+1;
        return x;
    }
    AVLNode leftRotate(AVLNode x){
        AVLNode y=x.right;AVLNode T2=y.left;
        y.left=x;x.right=T2;
        x.height=Math.max(height(x.left),height(x.right))+1;
        y.height=Math.max(height(y.left),height(y.right))+1;
        return y;
    }
    AVLNode insert(AVLNode node,Product p){
        if(node==null) return new AVLNode(p);
        if(p.id<node.p.id) node.left=insert(node.left,p);
        else if(p.id>node.p.id) node.right=insert(node.right,p);
        else return node; 
        node.height=1+Math.max(height(node.left),height(node.right));
        int bal=getBalance(node);
        if(bal>1 && p.id<node.left.p.id) return rightRotate(node);
        if(bal<-1 && p.id>node.right.p.id) return leftRotate(node);
        if(bal>1 && p.id>node.left.p.id){node.left=leftRotate(node.left);return rightRotate(node);}
        if(bal<-1 && p.id<node.right.p.id){node.right=rightRotate(node.right);return leftRotate(node);}
        return node;
    }
    void inOrder(AVLNode node,DefaultListModel<String> listModel){
        if(node==null) return;
        inOrder(node.left,listModel);
        listModel.addElement(node.p.toString());
        inOrder(node.right,listModel);
    }
    Product searchById(AVLNode node,int id){
        if(node==null) return null;
        if(node.p.id==id) return node.p;
        if(id<node.p.id) return searchById(node.left,id);
        return searchById(node.right,id);
    }
    void searchByName(AVLNode node,String name,ArrayList<Product> results){
        if(node==null) return;
        if(node.p.name.equalsIgnoreCase(name)) results.add(node.p);
        searchByName(node.left,name,results);
        searchByName(node.right,name,results);
    }
    void saveToFile(AVLNode node,BufferedWriter writer)throws IOException{
        if(node==null) return;
        saveToFile(node.left,writer);
        writer.write(node.p.id+" "+node.p.name+" "+node.p.price);
        writer.newLine();
        saveToFile(node.right,writer);
    }
    void loadFromFile(String filename){
        root=null;
        try(Scanner sc=new Scanner(new File(filename))){
            while(sc.hasNext()){
                int id=sc.nextInt(); String name=sc.next(); int price=sc.nextInt();
                root=insert(root,new Product(id,name,price));
            }
        }catch(Exception e){}
    }
    void saveAll(String filename){
        try(BufferedWriter writer=new BufferedWriter(new FileWriter(filename))){
            saveToFile(root,writer);
        }catch(Exception e){}
    }
}

class User{
    static int lastID=0;
    int id;
    String username,password;
    User(String u,String p,int id){this.username=u; this.password=p; this.id=id;}
    @Override public String toString(){return "ID:"+id+" "+username;}
}

class CartNode{
    Product p; int qty; CartNode next;
    CartNode(Product p,int qty){this.p=p; this.qty=qty; next=null;}
    @Override public String toString(){ return p.name + " x" + qty; }
}

class Order{
    int priority; Product p; int qty;
    Order(Product p,int qty,int priority){this.p=p; this.qty=qty; this.priority=priority;}
}

public class ECommerceGUI_1 extends JFrame{

    private final Color TEAL        = new Color(0,128,128);
    private final Color DARK_TEAL   = new Color(0,105,100);
    private final Color LIGHT_TEAL  = new Color(204,230,230);
    private final Color WHITE       = Color.WHITE;

    Font labelFont  = new Font("Segoe UI", Font.BOLD, 14);
    Font fieldFont  = new Font("Segoe UI", Font.PLAIN, 14);

    AVLTree tree=new AVLTree();
    ArrayList<User> users=new ArrayList<>();
    CartNode cartHead=null;
    PriorityQueue<Order> orderQueue=new PriorityQueue<>((a,b)->b.priority-a.priority);
    User currentUser=null;

    DefaultListModel<String> productListModel=new DefaultListModel<>();
    JList<String> productList=new JList<>(productListModel);

    public ECommerceGUI_1(){
        setTitle("E-Commerce Platform");
        setSize(900,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        applyThemeToUIManager();

        tree.loadFromFile("products.txt");
        loadUsers();
        showLogin();
    }

    private void applyThemeToUIManager(){
        UIManager.put("OptionPane.background", TEAL);
        UIManager.put("Panel.background", TEAL);
        UIManager.put("OptionPane.messageForeground", WHITE);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("OptionPane.message", createThemedLabel(""));
        UIManager.put("OptionPane.messageForeground", WHITE);

        UIManager.put("Button.background", DARK_TEAL);
        UIManager.put("Button.foreground", WHITE);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
    }

    void loadUsers(){
        users.clear();
        int maxID=0;
        try(Scanner sc=new Scanner(new File("users.txt"))){
            while(sc.hasNext()){
                String u=sc.next(); String p=sc.next(); int id=sc.nextInt();
                users.add(new User(u,p,id));
                if(id>maxID) maxID=id;
            }
            User.lastID=maxID;
        }catch(Exception e){}
    }

    void saveUsers(){
        try(BufferedWriter writer=new BufferedWriter(new FileWriter("users.txt"))){
            for(User u: users) writer.write(u.username+" "+u.password+" "+u.id+"\n");
        }catch(Exception e){}
    }

    void showLogin(){
        JPanel panel = new JPanel();
        panel.setBackground(TEAL);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");
        userLabel.setForeground(WHITE);
        passLabel.setForeground(WHITE);
        userLabel.setFont(labelFont);
        passLabel.setFont(labelFont);

        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);
        username.setBackground(LIGHT_TEAL);
        password.setBackground(LIGHT_TEAL);
        username.setFont(fieldFont);
        password.setFont(fieldFont);

        JButton loginBtn = createPrimaryButton("Login");
        JButton registerBtn = createPrimaryButton("Register");

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(username, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(password, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(TEAL);
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        panel.add(btnPanel, gbc);

        setContentPane(panel);
        revalidate();

        loginBtn.addActionListener(e->{
            String u = username.getText();
            String p = new String(password.getPassword());
            for(User user: users){
                if(user.username.equals(u) && user.password.equals(p)){
                    currentUser = user;
                    showMainMenu();
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, createThemedLabel("Invalid credentials!"), "Login Error", JOptionPane.ERROR_MESSAGE);
        });

        registerBtn.addActionListener(e->{
            String u=username.getText();
            String p=new String(password.getPassword());

            
            boolean exists = false;
            for(User user: users){
                if(user.username.equalsIgnoreCase(u)){
                    exists = true;
                    break;
                }
            }

            if(exists){
                JOptionPane.showMessageDialog(this, createThemedLabel("Username already exists! Please login."), "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int id=++User.lastID;
            users.add(new User(u,p,id));
            saveUsers();
            JOptionPane.showMessageDialog(this, createThemedLabel("User registered! You can now login."), "Registration Success", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private JButton createPrimaryButton(String text){
        JButton b = new JButton(text);
        b.setBackground(DARK_TEAL);
        b.setForeground(WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setPreferredSize(new Dimension(120,36));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(DARK_TEAL.darker(), 2));
        return b;
    }

    private JButton createSecondaryButton(String text){
        JButton b = new JButton(text);
        b.setBackground(LIGHT_TEAL);
        b.setForeground(DARK_TEAL);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setPreferredSize(new Dimension(120,36));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(DARK_TEAL.darker(), 2));
        return b;
    }

    void showMainMenu(){
        setSize(1000,700);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(TEAL);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15,20,15,20);
        gbc.anchor = GridBagConstraints.CENTER;

        String[] btnNames = {
            "Add Product","View Products","Search Product",
            "Add to Cart","View Cart","Remove from Cart",
            "Place Order","Process Order","Registered Users","Logout"
        };

        JButton[] buttons = new JButton[btnNames.length];

        for(int i=0;i<btnNames.length;i++){
            buttons[i] = new JButton(btnNames[i]);
            buttons[i].setBackground(LIGHT_TEAL);
            buttons[i].setForeground(DARK_TEAL);
            buttons[i].setFont(new Font("Segoe UI", Font.BOLD, 16));
            buttons[i].setFocusPainted(false);
            buttons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            buttons[i].setBorder(BorderFactory.createLineBorder(DARK_TEAL.darker(),2));
            buttons[i].setPreferredSize(new Dimension(220,45));

            final Color normalBg = LIGHT_TEAL;
            final Color hoverBg = new Color(181, 221, 216);

            buttons[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    ((JButton)e.getSource()).setBackground(hoverBg);
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    ((JButton)e.getSource()).setBackground(normalBg);
                }
            });

            gbc.gridx = i % 2;
            gbc.gridy = i / 2;
            panel.add(buttons[i], gbc);
        }

        setContentPane(panel);
        revalidate();

        buttons[0].addActionListener(e->addProductDialog());
        buttons[1].addActionListener(e->viewProductsDialog());
        buttons[2].addActionListener(e->searchProductDialog());
        buttons[3].addActionListener(e->addToCartDialog());
        buttons[4].addActionListener(e->viewCartDialog());
        buttons[5].addActionListener(e->removeFromCartDialog());

        buttons[6].addActionListener(e->placeOrderDialog());
        buttons[7].addActionListener(e->processOrderDialog());
        buttons[8].addActionListener(e->showUsersDialog());
        buttons[9].addActionListener(e->{currentUser=null; showLogin();});
    }


    void addProductDialog(){
        JDialog dialog = new JDialog(this, "Add Product", true);
        dialog.setSize(420,260);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(TEAL);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(TEAL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        JLabel l1 = new JLabel("Product ID:");
        JLabel l2 = new JLabel("Name:");
        JLabel l3 = new JLabel("Price:");
        for(JLabel l:new JLabel[]{l1,l2,l3}){ l.setFont(labelFont); l.setForeground(WHITE); }

        JTextField idField    = new JTextField(14);
        JTextField nameField  = new JTextField(14);
        JTextField priceField = new JTextField(14);

        JTextField[] fields = {idField,nameField,priceField};
        for(JTextField f:fields){
            f.setBackground(LIGHT_TEAL);
            f.setFont(fieldFont);
            f.setForeground(DARK_TEAL.darker());
        }

        gbc.gridx=0; gbc.gridy=0; gbc.anchor = GridBagConstraints.EAST; panel.add(l1,gbc);
        gbc.gridx=1; gbc.anchor = GridBagConstraints.WEST; panel.add(idField,gbc);
        gbc.gridx=0; gbc.gridy=1; gbc.anchor = GridBagConstraints.EAST; panel.add(l2,gbc);
        gbc.gridx=1; gbc.anchor = GridBagConstraints.WEST; panel.add(nameField,gbc);
        gbc.gridx=0; gbc.gridy=2; gbc.anchor = GridBagConstraints.EAST; panel.add(l3,gbc);
        gbc.gridx=1; gbc.anchor = GridBagConstraints.WEST; panel.add(priceField,gbc);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(TEAL);
        btnPanel.setBorder(new EmptyBorder(10,0,0,0));
        JButton ok = createPrimaryButton("OK");
        JButton cancel = createSecondaryButton("Cancel");
        ok.setPreferredSize(new Dimension(100,32));
        cancel.setPreferredSize(new Dimension(100,32));
        btnPanel.add(ok);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(cancel);

        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnPanel, gbc);

        dialog.setContentPane(panel);

        ok.addActionListener(e->{
            try{
                int id=Integer.parseInt(idField.getText().trim());
                String name=nameField.getText().trim();
                int price=Integer.parseInt(priceField.getText().trim());

                tree.root=tree.insert(tree.root,new Product(id,name,price));
                tree.saveAll("products.txt");
                JOptionPane.showMessageDialog(dialog, createThemedLabel("Product Added Successfully!"), "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }catch(Exception ex){
                JOptionPane.showMessageDialog(dialog, createThemedLabel("Invalid input. Product not added."), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e->dialog.dispose());

        dialog.setVisible(true);
    }

    void viewProductsDialog(){
        productListModel.clear();
        tree.inOrder(tree.root, productListModel);

        Color tealBg = TEAL;
        Color textColor = WHITE;

        productList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus){
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                if(isSelected){
                    lbl.setBackground(tealBg.darker());
                    lbl.setForeground(textColor);
                } else {
                    lbl.setBackground(tealBg);
                    lbl.setForeground(textColor);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });

        productList.setBackground(tealBg);
        productList.setForeground(textColor);

        JScrollPane scroll = new JScrollPane(productList);
        scroll.getViewport().setBackground(tealBg);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JDialog dialog = new JDialog(this, "All Products", true);
        dialog.setSize(600,450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(TEAL);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(tealBg);
        panel.setBorder(new EmptyBorder(15,15,15,15));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(tealBg);
        bottom.setBorder(new EmptyBorder(10,0,0,0));
        JButton close = createSecondaryButton("Close");
        close.setPreferredSize(new Dimension(100,30));
        close.addActionListener(e -> dialog.dispose());
        bottom.add(close);
        panel.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.getRootPane().setBackground(tealBg);
        dialog.setVisible(true);
    }

    void searchProductDialog(){
        String name = (String) JOptionPane.showInputDialog(
                this,
                createThemedLabel("Enter Product Name:"),
                "Search Product",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
        );
        if(name==null) return;

        ArrayList<Product> results = new ArrayList<>();
        tree.searchByName(tree.root,name,results);

        if(results.isEmpty()){
            JOptionPane.showMessageDialog(this,createThemedLabel("No Product Found!"), "Search Result", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextArea area = new JTextArea();
        area.setBackground(LIGHT_TEAL);
        area.setFont(new Font("Segoe UI",Font.PLAIN,14));
        area.setEditable(false);
        area.setForeground(DARK_TEAL.darker());

        for(Product p:results) area.append(p+"\n");

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.getViewport().setBackground(TEAL);

        JOptionPane.showMessageDialog(
                this,scrollPane,
                "Search Results",JOptionPane.PLAIN_MESSAGE);
    }

    private JLabel createThemedLabel(String text){
        JLabel l = new JLabel(text);
        l.setForeground(WHITE);
        l.setFont(labelFont);
        return l;
    }

    
    void addToCartDialog(){
        while(true){
            String name = JOptionPane.showInputDialog(
                    this,
                    createThemedLabel("Enter Product Name to Add to Cart (Cancel to stop):")
            );

            if(name == null) break;

            ArrayList<Product> results = new ArrayList<>();
            tree.searchByName(tree.root, name, results);

            if(results.isEmpty()){
                JOptionPane.showMessageDialog(this,createThemedLabel("Product not found!"), "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            Product selected;
            if(results.size() == 1){
                selected = results.get(0);
            } else {
                selected = (Product) JOptionPane.showInputDialog(
                        this,
                        createThemedLabel("Select Product:"),
                        "Multiple Products Found",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        results.toArray(),
                        results.get(0)
                );
            }

            if(selected == null) continue;

            String qtyStr = JOptionPane.showInputDialog(this,createThemedLabel("Enter Quantity:"));
            if(qtyStr == null) continue;

            int qty = 1;
            try{
                qty = Integer.parseInt(qtyStr);
                if(qty <= 0) throw new NumberFormatException();
            }catch(Exception ex){
                JOptionPane.showMessageDialog(this,createThemedLabel("Invalid quantity!"), "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            CartNode node = new CartNode(selected, qty);
            node.next = cartHead;
            cartHead = node;

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    createThemedLabel("Product added to cart!\nDo you want to add another product?"),
                    "Add More?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if(choice != JOptionPane.YES_OPTION){
                break;
            }
        }
    }

    void viewCartDialog(){
        if(cartHead==null){
            JOptionPane.showMessageDialog(this,createThemedLabel("Cart is empty!"), "Cart Status", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextArea area = new JTextArea(12,30);
        area.setBackground(LIGHT_TEAL);
        area.setFont(new Font("Segoe UI",Font.PLAIN,14));
        area.setEditable(false);
        area.setForeground(DARK_TEAL.darker());

        int total=0;
        for(CartNode t=cartHead;t!=null;t=t.next){
            area.append(t.p.name+" x"+t.qty+" = Rs."+t.p.price*t.qty+"\n");
            total+=t.p.price*t.qty;
        }
        area.append("\nTotal = Rs."+total);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.getViewport().setBackground(TEAL);

        JOptionPane.showMessageDialog(
                this,scrollPane,
                "Your Cart",JOptionPane.PLAIN_MESSAGE);
    }

    void removeFromCartDialog() {
        if(cartHead == null){
            JOptionPane.showMessageDialog(this, createThemedLabel("Cart is empty!"), "Cart Status", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ArrayList<CartNode> cartItems = new ArrayList<>();
        ArrayList<String> displayItems = new ArrayList<>();
        for(CartNode t = cartHead; t != null; t = t.next) {
            cartItems.add(t);
            displayItems.add(t.p.name + " x" + t.qty);
        }

        String selectedDisplay = (String) JOptionPane.showInputDialog(
                this,
                createThemedLabel("Select Product to Remove:"),
                "Remove from Cart",
                JOptionPane.PLAIN_MESSAGE,
                null,
                displayItems.toArray(),
                displayItems.get(0)
        );

        if(selectedDisplay == null) return;

        int selectedIndex = -1;
        for(int i = 0; i < displayItems.size(); i++){
            if(displayItems.get(i).equals(selectedDisplay)){
                selectedIndex = i;
                break;
            }
        }

        if(selectedIndex == -1) return;

        CartNode selectedNode = cartItems.get(selectedIndex);

        if(cartHead == selectedNode){
            cartHead = cartHead.next;
        } else {
            CartNode prev = cartHead;
            while(prev.next != null && prev.next != selectedNode) prev = prev.next;
            if(prev.next == selectedNode) prev.next = selectedNode.next;
        }

        JOptionPane.showMessageDialog(this, createThemedLabel("Removed: " + selectedNode.p.name), "Removal Success", JOptionPane.INFORMATION_MESSAGE);
    }

    void placeOrderDialog(){
        if(cartHead==null){
            JOptionPane.showMessageDialog(this,createThemedLabel("Cart is empty!"), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        orderQueue.add(new Order(cartHead.p,cartHead.qty,new Random().nextInt(10)));
        cartHead=cartHead.next;
        JOptionPane.showMessageDialog(this,createThemedLabel("Order placed!"), "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    void processOrderDialog(){
        Order o=orderQueue.poll();
        if(o==null) JOptionPane.showMessageDialog(this,createThemedLabel("No orders to process!"), "Order Status", JOptionPane.INFORMATION_MESSAGE);
        else JOptionPane.showMessageDialog(this,createThemedLabel("Processed Order: "+o.p+" x"+o.qty), "Order Processed", JOptionPane.INFORMATION_MESSAGE);
    }

    void showUsersDialog(){
        JTextArea area = new JTextArea(12,25);
        area.setBackground(LIGHT_TEAL);
        area.setFont(new Font("Segoe UI",Font.PLAIN,14));
        area.setEditable(false);
        area.setForeground(DARK_TEAL.darker());

        for(User u:users)
            area.append("ID: "+u.id+"   "+u.username+"\n");

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.getViewport().setBackground(TEAL);

        JOptionPane.showMessageDialog(
                this,scrollPane,
                "Registered Users",JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new ECommerceGUI_1().setVisible(true));
    }
}
