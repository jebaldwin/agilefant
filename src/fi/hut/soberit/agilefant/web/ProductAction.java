package fi.hut.soberit.agilefant.web;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork.Action;

import fi.hut.soberit.agilefant.business.ProductBusiness;
import fi.hut.soberit.agilefant.business.ProjectBusiness;
import fi.hut.soberit.agilefant.db.ProductDAO;
import fi.hut.soberit.agilefant.model.Product;
import flexjson.JSONSerializer;

public class ProductAction extends BacklogContentsAction implements CRUDAction {

    private static final long serialVersionUID = 1834399750050895118L;

    private ProductDAO productDAO;

//    private BacklogItemDAO backlogItemDAO;

    private ProjectBusiness projectBusiness;
    
    private ProductBusiness productBusiness;
    
    private int productId;

    private Product product;

    private Collection<Product> products = new ArrayList<Product>();
    
//    private Map<Project, EffortSumData> effLeftSums;
//    
//    private Map<Project, EffortSumData> origEstSums;
//    
//    private Map<BusinessTheme, BusinessThemeMetrics> themeMetrics;
    
    //private BusinessThemeBusiness businessThemeBusiness; 
    
    private String jsonData = "";
    
    

    public String create() {
        productId = 0;
        product = new Product();
        backlog = product;
        return Action.SUCCESS;
    }

    public String delete() {
        Product p = productDAO.get(productId);
        if (p == null) {
            super.addActionError(super.getText("product.notFound"));
            return Action.ERROR;
        }
//        if (p.getBacklogItems().size() > 0 || p.getProjects().size() > 0) {
//            super.addActionError(super.getText("product.notEmptyWhenDeleting"));
//            return Action.ERROR;
//        }
//        if (p.getBusinessThemes().size() > 0) {
//            super.addActionError(super.getText("product.notEmptyOfThemesWhenDeleting"));
//            return Action.ERROR;
//        }
        productDAO.remove(productId);
        return Action.SUCCESS;
    }

    public String edit() {
        // Date startDate = new Date(0);
        product = productBusiness.retrieve(productId);
        if (product == null) {
            super.addActionError(super.getText("product.notFound"));
            return Action.ERROR;
        }
        backlog = product;

        super.initializeContents();
        
        // Calculate product's projects' effort lefts and original estimates
        
//        effLeftSums = new HashMap<Project, EffortSumData>();
//        origEstSums = new HashMap<Project, EffortSumData>();
//        
//        // Calculate themes' done blis
//        themeMetrics = businessThemeBusiness.getThemeMetrics(productId);
//        
//        // Calculate projects' metrics.
//        projectBusiness.calculateProjectMetrics(product);
//        
//        Collection<Project> projects = product.getProjects();
//        
//        for (Project pro : projects) {
//            Collection<BacklogItem> blis = projectBusiness.getBlisInProjectAndItsIterations(pro);
//            EffortSumData projectEffLeftSum = backlogBusiness.getEffortLeftSum(blis);
//            EffortSumData projectOrigEstSum = backlogBusiness.getOriginalEstimateSum(blis);
//            effLeftSums.put(pro, projectEffLeftSum);
//            origEstSums.put(pro, projectOrigEstSum);
//        }
        
        return Action.SUCCESS;
    }

    public String store() {
        Product storable = new Product();

        if (productId > 0) {
            storable = productDAO.get(productId);
            if (storable == null) {
                super.addActionError(super.getText("product.notFound"));
                return CRUDAction.AJAX_ERROR;
            }
        }
        this.fillStorable(storable);
        if (super.hasActionErrors()) {
            return CRUDAction.AJAX_ERROR;
        }

        if (productId == 0) {
            productId = (Integer) backlogBusiness.create(storable);
        } else {
            backlogBusiness.store(storable);
        }
        return CRUDAction.AJAX_SUCCESS;
    }

    protected void fillStorable(Product storable) {
        if (this.product == null) {
            super.addActionError(super.getText("product.internalError"));
            return;
        }
        if (this.product.getName() == null || 
                this.product.getName().trim().equals("")) {
            super.addActionError(super.getText("product.missingName"));
            return;
        }
        storable.setName(this.product.getName());
        storable.setDescription(this.product.getDescription());
    }
    
    /**
     * @return a string of JSON serialized products 
     */
    public String getAllProductsAsJSON() {
        return new JSONSerializer().serialize(productBusiness.retrieveAll());
    }
    
    public String getProductAsJSON() {
        return new JSONSerializer().serialize(productBusiness.retrieve(productId));
    }
    
    /**
     * Get product data as JSON.
     * <p>
     * If given product id is greater than 0, return the product's data.
     * Otherwise, return all products' data.
     * @return product data as JSON string  
     */
    public String getProductJSON() {
        if (productId > 0) {
            jsonData = this.getProductAsJSON();
        }
        else {
            jsonData = this.getAllProductsAsJSON();
        }
        return Action.SUCCESS;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        this.backlog = product;
    }

    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    protected ProductDAO getProductDAO() {
        return this.productDAO;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Collection<Product> getProducts() {
        return products;
    }

    public void setProducts(Collection<Product> products) {
        this.products = products;
    }

    
//    public Map<Project, EffortSumData> getEffLeftSums() {
//        return effLeftSums;
//    }
//
//    public Map<Project, EffortSumData> getOrigEstSums() {
//        return origEstSums;
//    }

    public ProjectBusiness getProjectBusiness() {
        return projectBusiness;
    }

    public void setProjectBusiness(ProjectBusiness projectBusiness) {
        this.projectBusiness = projectBusiness;
    }
//
//    public void setEffLeftSums(Map<Project, EffortSumData> effLeftSums) {
//        this.effLeftSums = effLeftSums;
//    }
//
//    public void setOrigEstSums(Map<Project, EffortSumData> origEstSums) {
//        this.origEstSums = origEstSums;
//    }
//    
//    public Collection<BusinessTheme> getActiveBusinessThemes() {
//        return businessThemeBusiness.getActiveBusinessThemes(productId);
//    }
//
//    public Collection<BusinessTheme> getNonActiveBusinessThemes() {
//        return businessThemeBusiness.getNonActiveBusinessThemes(productId);
//    }
//
//    public Map<BusinessTheme, BusinessThemeMetrics> getBusinessThemeMetrics() {
//        return themeMetrics;
//    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    @Autowired
    public void setProductBusiness(ProductBusiness productBusiness) {
        this.productBusiness = productBusiness;
    }
}