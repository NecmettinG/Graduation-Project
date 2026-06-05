import React, { InputHTMLAttributes } from "react";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, className = "", id, ...props }, ref) => {
    const inputId = id || label.toLowerCase().replace(/\s+/g, '-');
    
    return (
      <div className={`input-group ${className}`}>
        <label htmlFor={inputId} className="input-label">
          {label}
        </label>
        <input
          id={inputId}
          ref={ref}
          className="input-field"
          {...props}
        />
        {error && <span style={{ color: 'red', fontSize: '0.75rem', marginTop: '0.25rem' }}>{error}</span>}
      </div>
    );
  }
);

Input.displayName = "Input";
