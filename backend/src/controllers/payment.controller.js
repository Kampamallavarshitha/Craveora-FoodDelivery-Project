// CASH ON DELIVERY

export const cashOnDelivery = async (req, res) => {

    try {

        res.status(200).json({

            success: true,

            message: "Cash on Delivery selected",

            paymentStatus: "Pending"

        });

    } catch (error) {

        res.status(500).json({

            success: false,

            message: error.message

        });

    }
};