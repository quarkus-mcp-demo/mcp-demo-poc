---
name: analyse-complaints-raise-tickets
description: Analyze last 50 complaints, identify top 3 product improvements, and create JIRA tickets in GLOBEX project
---

# Complaint Analysis & JIRA Ticket Creation Workflow

Follow these steps:

## Step 1: Read and Analyze Complaints
1. Query the complaints database to get the last 50 complaints
2. Analyze the complaints to identify the top 3 product improvements we should undertake
3. Group related complaints by the improvement theme
4. For each improvement, note the complaint IDs that support it

## Step 2: Present Findings
Display the analysis clearly:
- Top 3 product improvements (ranked by impact/frequency)
- For each improvement:
  - Description of the issue/opportunity
  - List of related complaint IDs
  - Recommended next course of action
  - Expected impact

## Step 3: Get Confirmation
Ask the user to confirm before creating JIRA tickets. Wait for explicit approval.

## Step 4: Create JIRA Tickets
Once confirmed, create 3 JIRA tickets on <username>.atlassian.net in the GLOBEX project:
- Use cloudId: "<username>.atlassian.net"
- projectKey: "GLOBEX"
- issueTypeName: "Task" (or "Story" if more appropriate)
- For each ticket include:
  - Clear summary of the product improvement
  - Description with:
    - Problem statement
    - Related complaint IDs (formatted as a list)
    - Recommended next course of action
    - Expected impact/benefit
  - Add appropriate labels if helpful (e.g., "customer-feedback", "product-improvement")

## Step 5: Confirm Completion
Provide the user with links to the created JIRA tickets.
