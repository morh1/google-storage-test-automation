package utils;

import com.google.cloud.billing.v1.BillingAccount;
import com.google.cloud.billing.v1.CloudBillingClient;
import com.google.cloud.billing.v1.ProjectBillingInfo;
import com.google.cloud.billing.v1.UpdateProjectBillingInfoRequest;

import java.io.IOException;

/**
 * Manages Google Cloud project billing configuration.
 * This utility class retrieves active billing accounts and links a Google Cloud project
 * to an available billing account.
 *
 * @author Mor Hanania
 * @version 1.0
 * @since 2025-03-06
 */
public class BillingManager {

    /**
     * Configures billing for a specified Google Cloud project.
     * This method finds an active billing account and links the project to it.
     *
     * @param projectId The ID of the Google Cloud project.
     */
    public static void configureProjectBilling(String projectId) {
        try {
            // Retrieve an active billing account
            String billingAccountId = getFirstBillingAccountId();
            if (billingAccountId == null) {
                throw new IllegalStateException("No active billing accounts found. Please create one.");
            }

            // Link project to the billing account
            linkProjectToBilling(projectId, billingAccountId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure project billing: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the first available active billing account.
     * Searches for open billing accounts and returns the first available one.
     *
     * @return The billing account ID if found, otherwise {@code null}.
     */
    private static String getFirstBillingAccountId() {
        try (CloudBillingClient billingClient = CloudBillingClient.create()) {
            for (BillingAccount account : billingClient.listBillingAccounts().iterateAll()) {
                if (account.getOpen()) {
                    return account.getName().split("/")[1]; // Extract correct ID
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving billing accounts: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Links a Google Cloud project to a specified billing account.
     * Updates the billing information for the project with the given billing account.
     *
     * @param projectId        The Google Cloud project ID.
     * @param billingAccountId The billing account ID to link to the project.
     */
    private static void linkProjectToBilling(String projectId, String billingAccountId) {
        try (CloudBillingClient billingClient = CloudBillingClient.create()) {
            String projectName = "projects/" + projectId;
            String fullBillingAccountName = "billingAccounts/" + billingAccountId;

            ProjectBillingInfo billingInfo = ProjectBillingInfo.newBuilder()
                    .setName(projectName)
                    .setBillingAccountName(fullBillingAccountName)
                    .build();

            UpdateProjectBillingInfoRequest request = UpdateProjectBillingInfoRequest.newBuilder()
                    .setName(projectName)
                    .setProjectBillingInfo(billingInfo)
                    .build();

            billingClient.updateProjectBillingInfo(request);
        } catch (Exception e) {
            throw new RuntimeException("Error linking project to billing account: " + e.getMessage(), e);
        }
    }
}
