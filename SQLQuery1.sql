CREATE TABLE Invoices (
    InvoiceID INT PRIMARY KEY IDENTITY(1,1),
    InvoiceDate DATETIME DEFAULT GETDATE(),
    TotalAmount DECIMAL(18, 2)
);