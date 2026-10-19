package com.example.data

import com.example.model.Question
import com.example.model.UgtModule
import com.example.model.UgtSubject

object CurriculumData {

    val assessmentQuestions: List<Question> = listOf(
        Question(
            id = 1,
            subject = "Physics",
            text = "An object of mass 5 kg moves in a circular path of radius 2 m with a constant speed of 4 m/s. What is the magnitude of the centripetal force acting on it?",
            options = listOf("20 N", "40 N", "10 N", "80 N"),
            correctIndex = 1,
            explanation = "Centripetal force F = (m * v²) / r = (5 * 16) / 2 = 80 / 2 = 40 N."
        ),
        Question(
            id = 2,
            subject = "Mathematics",
            text = "If f(x) = 3x² - 8x + 5, what is the derivative f'(x) evaluated at x = 3?",
            options = listOf("10", "14", "18", "6"),
            correctIndex = 0,
            explanation = "f'(x) = 6x - 8. At x = 3: f'(3) = 6(3) - 8 = 18 - 8 = 10."
        ),
        Question(
            id = 3,
            subject = "Chemistry",
            text = "Which of the following compounds displays sp² hybridization in all its carbon atoms?",
            options = listOf("Ethane (C2H6)", "Benzene (C6H6)", "Ethyne (C2H2)", "Methane (CH4)"),
            correctIndex = 1,
            explanation = "In Benzene (C6H6), each carbon atom forms 3 sigma bonds and participates in a conjugated pi system with sp² hybridization."
        ),
        Question(
            id = 4,
            subject = "Logical Aptitude",
            text = "Find the missing number in the progression: 3, 7, 15, 31, 63, ?",
            options = listOf("125", "127", "129", "131"),
            correctIndex = 1,
            explanation = "The pattern is (x * 2) + 1. Next term = (63 * 2) + 1 = 126 + 1 = 127."
        ),
        Question(
            id = 5,
            subject = "General Science",
            text = "What is the powerhouse organelle of the eukaryotic cell responsible for ATP cellular respiration?",
            options = listOf("Ribosome", "Golgi apparatus", "Mitochondria", "Lysosome"),
            correctIndex = 2,
            explanation = "Mitochondria generate most of the chemical energy needed to power the cell's biochemical reactions via ATP synthesis."
        )
    )

    val ugtSubjects: List<UgtSubject> = listOf(
        UgtSubject(
            id = "sub_phy",
            title = "Physics & Applied Mechanics",
            instructor = "Dr. R. K. Sharma (UGT Senior Faculty)",
            chapterCount = 12,
            progressPercent = 68,
            modules = listOf(
                UgtModule(
                    id = "mod_p1",
                    title = "Electromagnetism & Gauss's Law",
                    duration = "45 mins",
                    summary = "Electric flux through any closed surface is proportional to the total electric charge enclosed within that surface.",
                    keyFormulas = listOf(
                        "Φ = ∮ E · dA = Q_enclosed / ε₀",
                        "F = (1 / 4πε₀) * (q₁q₂ / r²)",
                        "E = kQ / r²"
                    ),
                    solvedExample = "Problem: A sphere has a charge of 3.54 μC at center. Calculate electric flux through surface.\nSolution: Φ = Q / ε₀ = 3.54×10⁻⁶ / 8.85×10⁻¹² = 4.0×10⁵ N·m²/C."
                ),
                UgtModule(
                    id = "mod_p2",
                    title = "Rotational Dynamics & Torque",
                    duration = "52 mins",
                    summary = "Angular momentum conservation, moment of inertia of symmetrical bodies, and rotational kinetic energy.",
                    keyFormulas = listOf(
                        "τ = I * α = r × F",
                        "L = I * ω",
                        "K_rot = 0.5 * I * ω²"
                    ),
                    solvedExample = "Problem: A solid disc with I = 0.2 kg·m² accelerates from rest to 30 rad/s in 6 seconds. Find torque.\nSolution: α = (30 - 0)/6 = 5 rad/s². τ = I * α = 0.2 * 5 = 1.0 N·m."
                )
            )
        ),
        UgtSubject(
            id = "sub_math",
            title = "Higher Mathematics & Calculus",
            instructor = "Prof. V. Gupta (IIT Kharagpur Alum)",
            chapterCount = 14,
            progressPercent = 75,
            modules = listOf(
                UgtModule(
                    id = "mod_m1",
                    title = "Definite Integrals & Area Under Curves",
                    duration = "58 mins",
                    summary = "Fundamental theorem of calculus, integration by parts, substitution, and standard curve boundaries.",
                    keyFormulas = listOf(
                        "∫ u dv = u v - ∫ v du",
                        "∫ₐᵇ f(x) dx = F(b) - F(a)",
                        "Area = ∫ |y₁ - y₂| dx"
                    ),
                    solvedExample = "Problem: Evaluate ∫₀¹ (3x² + 2x) dx.\nSolution: [x³ + x²] from 0 to 1 = (1 + 1) - 0 = 2."
                ),
                UgtModule(
                    id = "mod_m2",
                    title = "Vectors & 3D Coordinate Geometry",
                    duration = "40 mins",
                    summary = "Dot product, cross product, direction cosines, and equation of planes in 3D Euclidean space.",
                    keyFormulas = listOf(
                        "a · b = |a||b| cos θ",
                        "|a × b| = |a||b| sin θ",
                        "r · n = d"
                    ),
                    solvedExample = "Problem: Find dot product of a = 2i + 3j - k and b = i - j + 2k.\nSolution: a · b = (2)(1) + (3)(-1) + (-1)(2) = 2 - 3 - 2 = -3."
                )
            )
        ),
        UgtSubject(
            id = "sub_chem",
            title = "Organic & Physical Chemistry",
            instructor = "Prof. Ananya Sen (UGT Chemistry Head)",
            chapterCount = 10,
            progressPercent = 60,
            modules = listOf(
                UgtModule(
                    id = "mod_c1",
                    title = "Chemical Kinetics & Rate Laws",
                    duration = "38 mins",
                    summary = "Reaction order, Arrhenius equation, activation energy, and half-life for first-order reactions.",
                    keyFormulas = listOf(
                        "Rate = k [A]ᵐ [B]ⁿ",
                        "t_half = 0.693 / k (First Order)",
                        "k = A * e^(-Ea / RT)"
                    ),
                    solvedExample = "Problem: For a 1st order reaction with k = 0.0231 min⁻¹, calculate half-life.\nSolution: t_half = 0.693 / 0.0231 = 30 minutes."
                )
            )
        )
    )
}
